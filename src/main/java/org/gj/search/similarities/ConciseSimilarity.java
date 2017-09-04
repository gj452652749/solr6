package org.gj.search.similarities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

import com.gj.number.NumManager;

public class ConciseSimilarity extends Similarity{
	private static final float[] NORM_TABLE = new float[256];

	  static {
	    for (int i = 0; i < 256; i++) {
	      NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
	    }
	  }
	  
	  /**
	   * Sole constructor. (For invocation by subclass 
	   * constructors, typically implicit.)
	   */
	  public ConciseSimilarity() {}	  
	  /** Implemented as <code>overlap / maxOverlap</code>. */
	  @Override
	  public float coord(int overlap, int maxOverlap) {
	    return overlap / (float)maxOverlap;
	  }

	  /** Implemented as <code>1/sqrt(sumOfSquaredWeights)</code>. */
	  @Override
	  public float queryNorm(float sumOfSquaredWeights) {
	    return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
	  }
	  
	  public final long encodeNormValue(float f) {
	    return SmallFloat.floatToByte315(f);
	  }

	  public final float decodeNormValue(long norm) {
	    return NORM_TABLE[(int) (norm & 0xFF)];  // & 0xFF maps negative bytes to positive above 127
	  }
	  public float lengthNorm(FieldInvertState state) {
	    final int numTerms;
	    if (discountOverlaps)
	      numTerms = state.getLength() - state.getNumOverlap();
	    else
	      numTerms = state.getLength();
	    return state.getBoost() * ((float) (1.0 / Math.sqrt(numTerms)));
	  }

	  /** Implemented as <code>sqrt(freq)</code>. */
	  public float tf(float freq) {
	    return (float)Math.sqrt(freq);
	  }
	    
	  /** Implemented as <code>1 / (distance + 1)</code>. */
	  public float sloppyFreq(int distance) {
	    return 1.0f / (distance + 1);
	  }
	  
	  /** The default implementation returns <code>1</code> */
	  public float scorePayload(int doc, int start, int end, BytesRef payload) {
	    return 1;
	  }

	  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
	    final long df = termStats.docFreq();
	    final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
	    final float idf = idf(df, docCount);
	    return Explanation.match(idf, "idf, computed as log((docCount+1)/(docFreq+1)) + 1 from:",
	        Explanation.match(df, "docFreq"),
	        Explanation.match(docCount, "docCount"));
	  }

	  /** Implemented as <code>log((docCount+1)/(docFreq+1)) + 1</code>. */
	  public float idf(long docFreq, long docCount) {
		  return 1.0f;
	  }
	    
	  /** 
	   * True if overlap tokens (tokens with a position of increment of zero) are
	   * discounted from the document's length.
	   */
	  protected boolean discountOverlaps = true;

	  /** Determines whether overlap tokens (Tokens with
	   *  0 position increment) are ignored when computing
	   *  norm.  By default this is true, meaning overlap
	   *  tokens do not count when computing norms.
	   *
	   *  @lucene.experimental
	   *
	   *  @see #computeNorm
	   */
	  public void setDiscountOverlaps(boolean v) {
	    discountOverlaps = v;
	  }

	  /**
	   * Returns true if overlap tokens are discounted from the document's length. 
	   * @see #setDiscountOverlaps 
	   */
	  public boolean getDiscountOverlaps() {
	    return discountOverlaps;
	  }

	  @Override
	  public String toString() {
	    return "ClassicSimilarity";
	  }
	  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]) {
	    double idf = 0d; // sum into a double before casting into a float
	    List<Explanation> subs = new ArrayList<>();
	    for (final TermStatistics stat : termStats ) {
	      Explanation idfExplain = idfExplain(collectionStats, stat);
	      subs.add(idfExplain);
	      idf += idfExplain.getValue();
	    }
	    return Explanation.match((float) idf, "idf(), sum of:", subs);
	  }
	  @Override
	  public final long computeNorm(FieldInvertState state) {
	    float normValue = lengthNorm(state);
	    return encodeNormValue(normValue);
	  }	  
	  @Override
	  public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats) {
	    final Explanation idf = termStats.length == 1
	    ? idfExplain(collectionStats, termStats[0])
	    : idfExplain(collectionStats, termStats);
	    return new IDFStats(collectionStats.field(), idf);
	  }

	  @Override
	  public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
	    IDFStats idfstats = (IDFStats) stats;
	    return new TFIDFSimScorer(idfstats, context.reader().getNormValues(idfstats.field));
	  }
	  
	  private final class TFIDFSimScorer extends SimScorer {
	    private final IDFStats stats;
	    private final float weightValue;
	    private final NumericDocValues norms;
	    
	    TFIDFSimScorer(IDFStats stats, NumericDocValues norms) throws IOException {
	      this.stats = stats;
	      this.weightValue = stats.value;
	      this.norms = norms;
	    }
	    
	    @Override
	    public float score(int doc, float freq) {
	      final float raw = tf(freq) * weightValue; // compute tf(f)*weight
	      
	      return norms == null ? NumManager.getSignificantFigures(raw, 1) : NumManager.getSignificantFigures(raw * decodeNormValue(norms.get(doc)),1);  // normalize for field
	    }
	    
	    @Override
	    public float computeSlopFactor(int distance) {
	      return sloppyFreq(distance);
	    }

	    @Override
	    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
	      return scorePayload(doc, start, end, payload);
	    }

	    @Override
	    public Explanation explain(int doc, Explanation freq) {
	      return explainScore(doc, freq, stats, norms);
	    }
	  }
	  
	  /** Collection statistics for the TF-IDF model. The only statistic of interest
	   * to this model is idf. */
	  private static class IDFStats extends SimWeight {
	    private final String field;
	    /** The idf and its explanation */
	    private final Explanation idf;
	    private float queryNorm;
	    private float boost;
	    private float queryWeight;
	    private float value;
	    
	    public IDFStats(String field, Explanation idf) {
	      // TODO: Validate?
	      this.field = field;
	      this.idf = idf;
	      normalize(1f, 1f);
	    }

	    @Override
	    public float getValueForNormalization() {
	      // TODO: (sorta LUCENE-1907) make non-static class and expose this squaring via a nice method to subclasses?
	      return queryWeight * queryWeight;  // sum of squared weights
	    }

	    @Override
	    public void normalize(float queryNorm, float boost) {
	      this.boost = boost;
	      this.queryNorm = queryNorm;
	      queryWeight = queryNorm * boost * idf.getValue();
	      value = queryWeight * idf.getValue();         // idf for document
	    }
	  }  

	  private Explanation explainQuery(IDFStats stats) {
	    List<Explanation> subs = new ArrayList<>();

	    Explanation boostExpl = Explanation.match(stats.boost, "boost");
	    if (stats.boost != 1.0f)
	      subs.add(boostExpl);
	    subs.add(stats.idf);

	    Explanation queryNormExpl = Explanation.match(stats.queryNorm,"queryNorm");
	    subs.add(queryNormExpl);

	    return Explanation.match(
	        boostExpl.getValue() * stats.idf.getValue() * queryNormExpl.getValue(),
	        "queryWeight, product of:", subs);
	  }

	  private Explanation explainField(int doc, Explanation freq, IDFStats stats, NumericDocValues norms) {
	    Explanation tfExplanation = Explanation.match(tf(freq.getValue()), "tf(freq="+freq.getValue()+"), with freq of:", freq);
	    Explanation fieldNormExpl = Explanation.match(
	        norms != null ? decodeNormValue(norms.get(doc)) : 1.0f,
	        "fieldNorm(doc=" + doc + ")");

	    return Explanation.match(
	        tfExplanation.getValue() * stats.idf.getValue() * fieldNormExpl.getValue(),
	        "fieldWeight in " + doc + ", product of:",
	        tfExplanation, stats.idf, fieldNormExpl);
	  }

	  private Explanation explainScore(int doc, Explanation freq, IDFStats stats, NumericDocValues norms) {
	    Explanation queryExpl = explainQuery(stats);
	    Explanation fieldExpl = explainField(doc, freq, stats, norms);
	    if (queryExpl.getValue() == 1f) {
	      return fieldExpl;
	    }
	    return Explanation.match(
	        queryExpl.getValue() * fieldExpl.getValue(),
	        "score(doc="+doc+",freq="+freq.getValue()+"), product of:",
	        queryExpl, fieldExpl);
	  }

}

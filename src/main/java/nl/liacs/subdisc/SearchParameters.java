package nl.liacs.subdisc;

import java.util.*;

import nl.liacs.subdisc.ConditionListBuilder.ConditionList;

import org.w3c.dom.*;

/**
 * SearchParameters contains all search parameters for an experiment.
 */
public class SearchParameters implements XMLNodeInterface
{
	public static final float ALPHA_EDIT_DISTANCE = 0.0f;
	public static final float ALPHA_DEFAULT = 0.5f;
	public static final float BETA_DEFAULT = 1.0f;
	public static final int POST_PROCESSING_COUNT_DEFAULT = 20;

	// when adding/removing members be sure to update addNodeTo() and loadData()
	private TargetConcept	itsTargetConcept;
	private QM		itsQualityMeasure;
	private float		itsQualityMeasureMinimum;

	private int		itsSearchDepth;
	private int		itsMinimumCoverage;
	private float		itsMaximumCoverageFraction;
	private int		itsMinimumSupport;
	private int		itsMaximumSubgroups;
	private boolean		itsFilterSubgroups;
	private float		itsMinimumImprovement;
	private float		itsMaximumTime;

	private SearchStrategy	itsSearchStrategy;
	private int		itsSearchStrategyWidth;
	private boolean		itsNominalSets;
	private NumericOperatorSetting itsNumericOperatorSetting;
	private NumericStrategy	itsNumericStrategy;
	private int		itsNrBins;
	private int		itsNrThreads;

	private float		itsAlpha;
	private float		itsBeta;
	private boolean		itsPostProcessingDoAutoRun;
	private int		itsPostProcessingCount;

	private float		itsOverallRankingLoss;

	// TODO MM add to loadData() + autorun.dtd
	private List<ConditionList> itsBeamSeed;

	public SearchParameters(Node theSearchParametersNode)
	{
		loadData(theSearchParametersNode);
	}

	public SearchParameters()
	{
		/*
		 * There are no MiningWindow text fields for the following.
		 * But they need to be available for MULTI_LABELs
		 * 'Targets and Settings'.
		 * They are no longer 'static', but can be changed upon users
		 * discretion, therefore they can not be set in
		 * initSearchParameters.
		 */
		itsAlpha = ALPHA_DEFAULT;
		itsBeta = BETA_DEFAULT;
		itsPostProcessingCount = POST_PROCESSING_COUNT_DEFAULT;
		itsPostProcessingDoAutoRun = true;
	}

	// FIXME MM -> NONE OF THESE METHODS PERFORM INPUT VALIDATION
	/* QUALITY MEASURE */
	public TargetConcept getTargetConcept() { return itsTargetConcept; }
	public void setTargetConcept(TargetConcept theTargetConcept) { itsTargetConcept = theTargetConcept; }
	public TargetType getTargetType() { return itsTargetConcept.getTargetType(); }
	public QM getQualityMeasure() { return itsQualityMeasure; }
	public void setQualityMeasure(QM theQualityMeasure) { itsQualityMeasure = theQualityMeasure; }
	public float getQualityMeasureMinimum() { return itsQualityMeasureMinimum; }
	public void setQualityMeasureMinimum(float theQualityMeasureMinimum) { itsQualityMeasureMinimum = theQualityMeasureMinimum; }

	/* SEARCH CONDITIONS */
	public int getSearchDepth() { return itsSearchDepth; }
	public void setSearchDepth(int theSearchDepth) { itsSearchDepth = theSearchDepth; }
	public int getMinimumCoverage() { return itsMinimumCoverage; }
	public void setMinimumCoverage(int theMinimumCoverage) { if (theMinimumCoverage < 1) throw new IllegalArgumentException("theMinimumCoverage must be >= 1"); itsMinimumCoverage = theMinimumCoverage; }
	public float getMaximumCoverageFraction() { return itsMaximumCoverageFraction; }
	public void setMaximumCoverageFraction(float theMaximumCoverageFraction) { itsMaximumCoverageFraction = theMaximumCoverageFraction; }
	public int getMinimumSupport() { return itsMinimumSupport; }
	public void setMinimumSupport(int theMinimumSupport) { if (theMinimumSupport < 0) throw new IllegalArgumentException("theMinimumSupport must be >= 0"); itsMinimumSupport = theMinimumSupport; }
	public int getMaximumSubgroups() { return itsMaximumSubgroups; }
	public void setMaximumSubgroups(int theMaximumSubgroups) { itsMaximumSubgroups  = theMaximumSubgroups; }
	public boolean getFilterSubgroups()				{ return itsFilterSubgroups; }
	public void setFilterSubgroups(boolean theFilterSubgroups)	{ itsFilterSubgroups = theFilterSubgroups; }
	public float getMinimumImprovement() { return itsMinimumImprovement; }
	public void setMinimumImprovement(float theMinimumImprovement) { itsMinimumImprovement = theMinimumImprovement; }
	public float getMaximumTime() { return itsMaximumTime; }
	public void setMaximumTime(float theMaximumTime) { itsMaximumTime = theMaximumTime; }

	/* SEARCH STRATEGY */
	public SearchStrategy getSearchStrategy() { return itsSearchStrategy; }

	public void setSearchStrategy(String theSearchStrategyName)
	{
		itsSearchStrategy = SearchStrategy.fromString(theSearchStrategyName);
	}

	public void setSearchStrategy(SearchStrategy theSearchStrategy)
	{
		itsSearchStrategy = theSearchStrategy;
	}

	public boolean getNominalSets()
	{
		if (itsTargetConcept.getTargetType() != TargetType.SINGLE_NOMINAL) //other than SINGLE_NOMINAL?
			return false;
		else
			return itsNominalSets;
	}
	public void setNominalSets(boolean theValue) {itsNominalSets = theValue;}

	// FIXME MM - itsNumericStrategy should be set to 'in' when applicable
	public NumericOperatorSetting getNumericOperatorSetting()
	{
		if (itsNumericStrategy.isForHalfInterval())
			return itsNumericOperatorSetting;
		else
			return NumericOperatorSetting.INTERVALS;
	}

	public void setNumericOperators(String theNumericOperatorsName)
	{
		itsNumericOperatorSetting = NumericOperatorSetting.fromString(theNumericOperatorsName);
	}

	public void setNumericOperators(NumericOperatorSetting theNumericOperators)
	{
		itsNumericOperatorSetting = theNumericOperators;
	}

	public NumericStrategy getNumericStrategy() { return itsNumericStrategy; }
	public void setNumericStrategy(String theNumericStrategyName)
	{
		itsNumericStrategy = NumericStrategy.fromString(theNumericStrategyName);
	}
	public void setNumericStrategy(NumericStrategy theNumericStrategy)
	{
		itsNumericStrategy = theNumericStrategy;
	}

	public int getSearchStrategyWidth()				{ return itsSearchStrategyWidth; }
	public void setSearchStrategyWidth(int theWidth)		{ itsSearchStrategyWidth = theWidth; }
	public int getNrBins()						{ return itsNrBins; }
	public void setNrBins(int theNrBins)				{ itsNrBins = theNrBins; }
	public int getNrThreads()					{ return itsNrThreads; }
	public void setNrThreads(int theNrThreads)			{ itsNrThreads = theNrThreads; }
	public float getAlpha()						{ return itsAlpha; }
	public void setAlpha(float theAlpha)				{ itsAlpha = theAlpha; }
	public float getBeta()						{ return itsBeta; }
	public void setBeta(float theBeta)				{ itsBeta = theBeta; }
	public boolean getPostProcessingDoAutoRun()			{ return itsPostProcessingDoAutoRun; }
	public void setPostProcessingDoAutoRun(boolean theAutoRunSetting)	{ itsPostProcessingDoAutoRun = theAutoRunSetting; }
	public int getPostProcessingCount()				{ return itsPostProcessingCount; }
	public void setPostProcessingCount(int theNr)			{ itsPostProcessingCount = theNr; }
//	public List<ConditionList> getBeamSeed()			{ return itsBeamSeed; }
//	public void setBeamSeed(List<ConditionList> theBeamSeed)	{ itsBeamSeed = theBeamSeed; }
	public List<ConditionList> getBeamSeed()			{ return itsBeamSeed; }
	public void setBeamSeed(List<ConditionList> theBeamSeed)	{ itsBeamSeed = theBeamSeed; }
	public float getOverallRankingLoss()				{ return itsOverallRankingLoss; }
	public void setOverallRankingLoss(float theOverallRankingLoss)	{ itsOverallRankingLoss = theOverallRankingLoss; }

	/**
	 * Creates an {@link XMLNode XMLNode} representation of this
	 * SearchParameters.
	 * @param theParentNode the Node of which this Node will be a ChildNode
//	 * @return a Node that contains all the information of this SearchParameters
	 */
	@Override
	public void addNodeTo(Node theParentNode)
	{
		Node aNode = XMLNode.addNodeTo(theParentNode, "search_parameters");
		// itsTargetConcept is added through its own Node
		XMLNode.addNodeTo(aNode, "quality_measure", itsQualityMeasure.GUI_TEXT);
		XMLNode.addNodeTo(aNode, "quality_measure_minimum", getQualityMeasureMinimum());
		XMLNode.addNodeTo(aNode, "search_depth", getSearchDepth());
		XMLNode.addNodeTo(aNode, "minimum_coverage", getMinimumCoverage());
		XMLNode.addNodeTo(aNode, "maximum_coverage_fraction", getMaximumCoverageFraction());
		XMLNode.addNodeTo(aNode, "minimum_support", getMinimumSupport());
		XMLNode.addNodeTo(aNode, "maximum_subgroups", getMaximumSubgroups());
		XMLNode.addNodeTo(aNode, "filter_subgroups", getFilterSubgroups());
		XMLNode.addNodeTo(aNode, "minimum_improvement", getMinimumImprovement());
		XMLNode.addNodeTo(aNode, "maximum_time", getMaximumTime());
		XMLNode.addNodeTo(aNode, "search_strategy", getSearchStrategy().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "use_nominal_sets", getNominalSets());
		XMLNode.addNodeTo(aNode, "search_strategy_width", getSearchStrategyWidth());
		XMLNode.addNodeTo(aNode, "numeric_operators", getNumericOperatorSetting().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "numeric_strategy", getNumericStrategy().GUI_TEXT);
		XMLNode.addNodeTo(aNode, "nr_bins", getNrBins());
		XMLNode.addNodeTo(aNode, "nr_threads", getNrThreads());
		XMLNode.addNodeTo(aNode, "alpha", getAlpha());
		XMLNode.addNodeTo(aNode, "beta", getBeta());
		XMLNode.addNodeTo(aNode, "post_processing_do_autorun", getPostProcessingDoAutoRun());
		XMLNode.addNodeTo(aNode, "post_processing_count", getPostProcessingCount());
		XMLNode.addNodeTo(aNode, "beam_seed", getBeamSeed());
		XMLNode.addNodeTo(aNode, "overall_ranking_loss", getOverallRankingLoss());
	}

	private void loadData(Node theSearchParametersNode)
	{
		NodeList aChildren = theSearchParametersNode.getChildNodes();
		for(int i = 0, j = aChildren.getLength(); i < j; ++i)
		{
			Node aSetting = aChildren.item(i);
			String aNodeName = aSetting.getNodeName().toLowerCase();
			if("quality_measure".equalsIgnoreCase(aNodeName))
				itsQualityMeasure = QM.fromString(aSetting.getTextContent());
			else if("quality_measure_minimum".equalsIgnoreCase(aNodeName))
				itsQualityMeasureMinimum = Float.parseFloat(aSetting.getTextContent());
			else if("search_depth".equalsIgnoreCase(aNodeName))
				itsSearchDepth = Integer.parseInt(aSetting.getTextContent());
			else if("minimum_coverage".equalsIgnoreCase(aNodeName))
				itsMinimumCoverage = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_coverage_fraction".equalsIgnoreCase(aNodeName))
				itsMaximumCoverageFraction = Float.parseFloat(aSetting.getTextContent());
			else if("minimum_support".equalsIgnoreCase(aNodeName))
				itsMinimumSupport = Integer.parseInt(aSetting.getTextContent());
			else if("maximum_subgroups".equalsIgnoreCase(aNodeName))
				itsMaximumSubgroups = Integer.parseInt(aSetting.getTextContent());
			else if("filter_subgroups".equalsIgnoreCase(aNodeName))
				itsFilterSubgroups = Boolean.parseBoolean(aSetting.getTextContent());
			else if("minimum_improvement".equalsIgnoreCase(aNodeName))
				itsMinimumImprovement = Float.parseFloat(aSetting.getTextContent());
			else if("maximum_time".equalsIgnoreCase(aNodeName))
				itsMaximumTime = Float.parseFloat(aSetting.getTextContent());
			else if("search_strategy".equalsIgnoreCase(aNodeName))
				itsSearchStrategy = (SearchStrategy.fromString(aSetting.getTextContent()));
			else if("use_nominal_sets".equalsIgnoreCase(aNodeName))
				itsNominalSets = Boolean.parseBoolean(aSetting.getTextContent());
			else if("search_strategy_width".equalsIgnoreCase(aNodeName))
			{
				String s = aSetting.getTextContent();
				if (s.length() == 0)
					itsSearchStrategyWidth = Integer.MIN_VALUE;
				else
					itsSearchStrategyWidth = Integer.parseInt(s);
			}
			else if("numeric_operators".equalsIgnoreCase(aNodeName))
				itsNumericOperatorSetting = (NumericOperatorSetting.fromString(aSetting.getTextContent()));
			else if("numeric_strategy".equalsIgnoreCase(aNodeName))
				itsNumericStrategy = (NumericStrategy.fromString(aSetting.getTextContent()));
			else if("nr_bins".equalsIgnoreCase(aNodeName))
				itsNrBins = Integer.parseInt(aSetting.getTextContent());
			else if("nr_threads".equalsIgnoreCase(aNodeName))
				itsNrThreads = Integer.parseInt(aSetting.getTextContent());
			else if("alpha".equalsIgnoreCase(aNodeName))
				itsAlpha = Float.parseFloat(aSetting.getTextContent());
			else if("beta".equalsIgnoreCase(aNodeName))
				itsBeta = Float.parseFloat(aSetting.getTextContent());
			else if("post_processing_do_autorun".equalsIgnoreCase(aNodeName))
				itsPostProcessingDoAutoRun = Boolean.parseBoolean(aSetting.getTextContent());
			else if("post_processing_count".equalsIgnoreCase(aNodeName))
				itsPostProcessingCount = Integer.parseInt(aSetting.getTextContent());
			else if("beam_seed".equalsIgnoreCase(aNodeName))
				itsBeamSeed = null; // FIXME MM see multi-targets
			else if("overall_ranking_loss".equalsIgnoreCase(aNodeName))
				itsOverallRankingLoss = Float.parseFloat(aSetting.getTextContent());
			else
				Log.logCommandLine("ignoring unknown XML node: " + aNodeName);
		}
	}

	// lame, similar to addNodeTo(Node)
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(1024);
		sb.append("search_parameters\n");

		addLine(sb, "quality_measure", itsQualityMeasure.GUI_TEXT);
		addLine(sb, "quality_measure_minimum", Float.toString(getQualityMeasureMinimum()));
		addLine(sb, "search_depth", Integer.toString(getSearchDepth()));
		addLine(sb, "minimum_coverage", Integer.toString(getMinimumCoverage()));
		addLine(sb, "maximum_coverage_fraction", Float.toString(getMaximumCoverageFraction()));
		addLine(sb, "minimum_support", Integer.toString(getMinimumSupport()));
		addLine(sb, "maximum_subgroups", Integer.toString(getMaximumSubgroups()));
		addLine(sb, "minimum_improvement", Float.toString(getMinimumImprovement()));
		addLine(sb, "filter_subgroups", Boolean.toString(getFilterSubgroups()));
		addLine(sb, "maximum_time", Float.toString(getMaximumTime()));
		addLine(sb, "search_strategy", getSearchStrategy().GUI_TEXT);
		addLine(sb, "use_nominal_sets", Boolean.toString(getNominalSets()));
		addLine(sb, "search_strategy_width", Integer.toString(getSearchStrategyWidth()));
		addLine(sb, "numeric_operators", getNumericOperatorSetting().getOperators().toString());
		addLine(sb, "numeric_strategy", getNumericStrategy().GUI_TEXT);
		addLine(sb, "nr_bins", Integer.toString(getNrBins()));
		addLine(sb, "nr_threads", Integer.toString(getNrThreads()));
		addLine(sb, "alpha", Float.toString(getAlpha()));
		addLine(sb, "beta", Float.toString(getBeta()));
		addLine(sb, "post_processing_do_autorun", Boolean.toString(getPostProcessingDoAutoRun()));
		addLine(sb, "post_processing_count", Integer.toString(getPostProcessingCount()));
		addLine(sb, "beam_seed", getBeamSeed() == null ? "[]" : getBeamSeed().toString());
		addLine(sb, "overall_ranking_loss", Float.toString(getOverallRankingLoss()));

		return sb.toString();
	}

	static final void addLine(StringBuilder builder, String setting, String value)
	{
		builder.append("\t").append(setting).append(" = ").append(value).append("\n");
	}
}

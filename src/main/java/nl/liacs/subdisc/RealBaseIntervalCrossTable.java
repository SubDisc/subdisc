package nl.liacs.subdisc;

import java.util.*;

import nl.liacs.subdisc.Column.ValueCountTP;

//Michael says: this is basically a copy of NominalCrossTable

public class RealBaseIntervalCrossTable
{
	private final float[] itsSplitPoints;
	private int itsSplitPointCount;     // not final, see aggregateIntervals()
	private final int[] itsPositiveCounts;
	private final int[] itsNegativeCounts;
	private final int itsPositiveCount; // sum
	private final int itsNegativeCount; // sum
	private final boolean itsUseNegInfty;

	public RealBaseIntervalCrossTable(int theTotalCount, int theTotalNrTruePositives, float[] theDomainCopy, ValueCountTP theValueInfo)
	{
		itsPositiveCount = theTotalNrTruePositives;
		itsNegativeCount = (theTotalCount - theTotalNrTruePositives);

		// compact arrays first, count aNrDistinct, array-copy; (abuse counts)
		int[] aCounts = theValueInfo.itsCounts;
		int[] aTPs    = theValueInfo.itsTruePositives;

		int aNrDistinct = 0;
		for (int i = 0, j = aCounts.length; i < j; ++i)
		{
			int aCount = aCounts[i];
			if (aCount == 0)
				continue;

			theDomainCopy[aNrDistinct] = theDomainCopy[i];
			aTPs[aNrDistinct]          = aTPs[i];
			aCounts[aNrDistinct]       = (aCount - aTPs[i]);
			++aNrDistinct;

			if ((theTotalCount -= aCount) == 0)
				break;
		}

		itsUseNegInfty = true;
		int offset     = itsUseNegInfty ? 1 : 0;

		itsSplitPointCount = aNrDistinct + offset;
		itsSplitPoints     = new float[itsSplitPointCount];
		itsPositiveCounts  = new int[getNrBaseIntervals()];
		itsNegativeCounts  = new int[getNrBaseIntervals()];

		if (itsUseNegInfty)
			itsSplitPoints[0] = Float.NEGATIVE_INFINITY;

		System.arraycopy(theDomainCopy, 0, itsSplitPoints,    offset, aNrDistinct);
		System.arraycopy(aTPs,          0, itsPositiveCounts, offset, aNrDistinct);
		System.arraycopy(aCounts,       0, itsNegativeCounts, offset, aNrDistinct);
	}

//	public RealBaseIntervalCrossTable(float[] theSplitPoints, Column theColumn, BitSet theSubgroupMembers, BitSet theTarget)
//	{
//		this(theSplitPoints, theColumn, theSubgroupMembers, theTarget, true);
//	}

//	private RealBaseIntervalCrossTable(float[] theSplitPoints, Column theColumn, BitSet theSubgroupMembers, BitSet theTarget, boolean theUseNegInfty)
//	{
//		itsUseNegInfty = theUseNegInfty;
//		int offset = itsUseNegInfty ? 1 : 0;
//		itsSplitPointCount = theSplitPoints.length + offset;
//		itsSplitPoints = new float[itsSplitPointCount];
//		itsPositiveCounts = new int[getNrBaseIntervals()];
//		itsNegativeCounts = new int[getNrBaseIntervals()];
//
//		int aCount = 0;
//		if (itsUseNegInfty)
//		{
//			itsSplitPoints[0] = Float.NEGATIVE_INFINITY;
//			aCount = 1;
//		}
//		for (float aSplitPoint : theSplitPoints)
//		{
//			itsSplitPoints[aCount] = aSplitPoint;
//			aCount++;
//		}
//		//sort(itsSplitPoints);
//
//		// FIXME half-interval code determines TP/FP also, but 70 times faster
//		for (int i=0; i<theColumn.size(); i++) //loop over all records (AK could be faster? ok for now)
//		{
//			if (theSubgroupMembers.get(i))
//			{
//				float aValue = theColumn.getFloat(i);
//				int anIndex = Arrays.binarySearch(itsSplitPoints, aValue);
//				if (anIndex < 0)
//					anIndex = -anIndex - 1;
//				if (theTarget.get(i))
//					itsPositiveCounts[anIndex]++;
//				else
//					itsNegativeCounts[anIndex]++;
//			}
//		}
//		for (int i=0; i<getNrBaseIntervals(); i++)
//		{
//			itsPositiveCount += itsPositiveCounts[i];
//			itsNegativeCount += itsNegativeCounts[i];
//		}
//
////		// always create a copy of the theSplitPoints input array
////		if (itsUseNegInfty)
////			itsSplitPoints[0] = Float.NEGATIVE_INFINITY;
////		System.arraycopy(theSplitPoints, 0, itsSplitPoints, offset, theSplitPoints.length);
////
////		final BitSet b = theSubgroup.getMembers();
////		// uses j to break loop as early as possible
////		for (int i = b.nextSetBit(0), j = theSubgroup.getCoverage(); j != 0 ; i = b.nextSetBit(i+1), --j) {
////			int anIndex = Arrays.binarySearch(itsSplitPoints, theColumn.getFloat(i));
////			if (anIndex < 0)
////				anIndex ~= anIndex; // bitwise inverse: -anIndex - 1;
////			if (theTarget.get(i))
////			{
////				++itsPositiveCounts[anIndex];
////				++itsPositiveCount;
////			}
////			else
////			{
////				++itsNegativeCounts[anIndex];
////				++itsNegativeCount;
////			}
////		}
//	}

	public float getSplitPoint(int theIndex)
	{
		return itsSplitPoints[theIndex];
	}

	// leave this in, used in evaluateNumericBestInterval disabled WRACC code
	public Interval getBaseInterval(int theIndex)
	{
		if (itsSplitPointCount == 0)
			return new Interval(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		if (theIndex == 0)
			return new Interval(Float.NEGATIVE_INFINITY, itsSplitPoints[0]);
		else if (theIndex == itsSplitPointCount)
			return new Interval(itsSplitPoints[theIndex-1], Float.POSITIVE_INFINITY);
		else
			return new Interval(itsSplitPoints[theIndex-1], itsSplitPoints[theIndex]);
	}

	public int getPositiveCount(int theIndex)
	{
		return itsPositiveCounts[theIndex];
	}

	public int getNegativeCount(int theIndex)
	{
		return itsNegativeCounts[theIndex];
	}

	public int getPositiveCount()
	{ 
		return itsPositiveCount;
	}

	public int getNegativeCount()
	{
		return itsNegativeCount;
	}

	public int getNrSplitPoints()
	{
		return itsSplitPointCount;
	}

	public int getNrBaseIntervals()
	{
		return itsSplitPointCount + 1; 
	}

	// leave this in, used in evaluateNumericBestInterval brute force test code
	public float[] getSplitPoints()
	{
		return Arrays.copyOfRange(itsSplitPoints, 0, itsSplitPointCount);
	}

	// eliminate split points that separate base intervals with equal distributions
	// only to be used for convex quality measures
	public void aggregateIntervals()
	{
		int aPruneCnt = 0;
		for (int i = (itsUseNegInfty ? 1: 0); i < itsSplitPointCount; i++)
		{
			// MM - multiplication overflow not a problem, essential part is: ==
			if ( itsPositiveCounts[i] * itsNegativeCounts[i+1] == itsPositiveCounts[i+1] * itsNegativeCounts[i] )
			{
				itsPositiveCounts[i-aPruneCnt] += itsPositiveCounts[i+1];
				itsNegativeCounts[i-aPruneCnt] += itsNegativeCounts[i+1];
				aPruneCnt++;
			}
			else if (aPruneCnt > 0)
			{
				itsPositiveCounts[i-aPruneCnt+1] = itsPositiveCounts[i+1];
				itsNegativeCounts[i-aPruneCnt+1] = itsNegativeCounts[i+1];
				itsSplitPoints[i-aPruneCnt] = itsSplitPoints[i];
			}
		}

		itsSplitPointCount -= aPruneCnt;
	}

//	public void print()
//	{
//		for (int i = 0; i < getNrBaseIntervals(); i++)
//			Log.logCommandLine(getBaseInterval(i) + ": (" + itsPositiveCounts[i] + ", " + itsNegativeCounts[i] + ")");
//		return;
//	}
}

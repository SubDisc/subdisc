package nl.liacs.subdisc;

import java.util.*;

/*
 * In most cases it would be useful for BinaryTable to remember the Columns it
 * represents. However, not all BinaryTables are build from Table Columns. For
 * example when the Subgroups are turned into binary Columns.
 */
public class BinaryTable
{
	private List<BitSet> itsColumns;
	private int itsNrRecords; //Nr. of examples

	//From Table
	public BinaryTable(Table theTable, List<Column> theColumns)
	{
		itsColumns = new ArrayList<BitSet>(theColumns.size());
		for (Column aColumn : theColumns)
			itsColumns.add(aColumn.getBinaries());
		itsNrRecords = theTable.getNrRows();
	}

	//turn subgroups into binary columns
	public BinaryTable(Table theTable, SubgroupSet theSubgroups)
	{
		itsColumns = new ArrayList<BitSet>(theSubgroups.size());
		itsNrRecords = theTable.getNrRows();

		for (Subgroup aSubgroup : theSubgroups)
		{
			BitSet aColumn = aSubgroup.getMembers();
			itsColumns.add(aColumn);
		}
	}

	//this assumes that theTargets is an ArrayList<BitSet>
	// internal use only: #selectColumns(ItemSet) and #selectRows(BitSet)
	private BinaryTable(List<BitSet> theTargets, int theNrRecords)
	{
		assert (theTargets instanceof ArrayList);

		itsColumns = theTargets;
		itsNrRecords = theNrRecords;
	}

	public BitSet getRow(int theIndex)
	{
		int itsColumnsSize = itsColumns.size();
		BitSet aBitSet = new BitSet(itsColumnsSize);
		for (int i = 0; i < itsColumnsSize; i++)
			aBitSet.set(i, itsColumns.get(i).get(theIndex));

		return aBitSet;
	}

	public BinaryTable selectColumns(ItemSet theItemSet)
	{
		List<BitSet> aNewTargets = new ArrayList<BitSet>(theItemSet.getItemCount());

		for (int i = 0; i < theItemSet.getDimensions(); i++)
			if (theItemSet.get(i))
				aNewTargets.add(itsColumns.get(i));

		return new BinaryTable(aNewTargets, itsNrRecords);
	}

	public BinaryTable selectRows(BitSet theMembers)
	{
		int aNrMembers = theMembers.cardinality();
		List<BitSet> aNewTargets = new ArrayList<BitSet>(getNrColumns());

		// single loop to get all indices of all set bits
//		int[] setBits = new int[aNrMembers];
//		for (int i = theMembers.nextSetBit(0), j = -1; i >=0; i = theMembers.nextSetBit(i+1))
//			setBits[++j] = i;
//	
//		for (BitSet aColumn : itsColumns)
//		{
//			BitSet aSmallerTarget = new BitSet(aNrMembers);
//			// set bits only when set in both theMembers and aColumn
//			for (int i = 0, j = setBits.length, k = -1; i < j; ++i)
//				if (aColumn.get(setBits[i]))
//					aSmallerTarget.set(++k);
//	
//			aNewTargets.add(aSmallerTarget);
//		}

		//copy targets
		for (BitSet aColumn : itsColumns)
		{
			BitSet aSmallerTarget = new BitSet(aNrMembers);
			int k=0;
			for (int j=0; j<getNrRecords(); j++)
				if (theMembers.get(j))
				{
					if (aColumn.get(j))
						aSmallerTarget.set(k);
					k++;
				}
			aNewTargets.add(aSmallerTarget);
		}

		return new BinaryTable(aNewTargets, aNrMembers);
	}

	public double computeBDeuFaster()
	{
		int aDimensions = itsColumns.size();

		// Init crosscube
		int aSize = (int)Math.pow(2, aDimensions);
		int[] aCounts = new int[aSize];
		int aTotalCount = 0;

		// Cache powers
		int[] aPowers = new int[aDimensions];
		for (int j = 0; j < aDimensions; j++)
			aPowers[j] = (int)Math.pow(2, aDimensions-j-1);

		// Fill crosscube
		for (int i = 0; i < itsNrRecords ; i++)
		{
			int anIndex = 0;
			for (int j = 0; j < aDimensions; j++)
				if(itsColumns.get(j).get(i))
					anIndex += aPowers[j];
			aCounts[anIndex]++;
			aTotalCount++;
		}

		// Compute BDeu
		if (aTotalCount == 0)
			return 0;

		double aQuality = 0.0;
		int q_i = aSize / 2;
		double alpha_ijk = 1.0 / (double) aSize;
		double alpha_ij  = 1.0 / (double) q_i;
		double LogGam_alpha_ijk = Function.logGamma(alpha_ijk); //uniform prior BDeu metric
		double LogGam_alpha_ij = Function.logGamma(alpha_ij);

		for (int j=0; j<q_i; j++)
		{
			double aSum = 0.0;
			double aPost = 0.0;

			//child = 0;
			aPost += Function.logGamma(alpha_ijk + aCounts[j*2]) - LogGam_alpha_ijk;
			aSum += aCounts[j*2];
			//child = 1;
			aPost += Function.logGamma(alpha_ijk + aCounts[j*2 + 1]) - LogGam_alpha_ijk;
			aSum += aCounts[j*2 + 1];

			aQuality += LogGam_alpha_ij - Function.logGamma(alpha_ij + aSum) + aPost;
		}
		return aQuality;
	}

	public ItemSet getApproximateMiki(int k)
	{
		long aCount = 0;
		ItemSet aMaximallyInformativeItemSet = new ItemSet(getNrColumns(), 0);
		double aMaximalEntropy = 0.0;

		Log.logCommandLine("finding approximate " + k + "-itemsets");
		for (int i=1; i<=k; i++)
		{
			ItemSet aTempItemSet = aMaximallyInformativeItemSet;
			for (int j=0; j<getNrColumns(); j++)
			{
				if (!aMaximallyInformativeItemSet.get(j))
				{
					aCount++;
					ItemSet anItemSet = aMaximallyInformativeItemSet.getExtension(j);
					BinaryTable aTable = selectColumns(anItemSet);
					CrossCube aCube = aTable.countCrossCube();
					double anEntropy = aCube.getEntropy();

					if (aMaximalEntropy < anEntropy)
					{
						aTempItemSet = anItemSet;
						aMaximalEntropy = anEntropy;
						Log.logCommandLine("found a new maximum: " + anItemSet + ": " + aMaximalEntropy);
					}
				}
			}
			// FIXME MM break if (aMaximallyInformativeItemSet == aTempItemSet)
			if (aMaximallyInformativeItemSet == aTempItemSet)
				Log.logCommandLine("no change for i = " + i);
			aMaximallyInformativeItemSet = aTempItemSet;
		}
		aMaximallyInformativeItemSet.setJointEntropy(aMaximalEntropy);

		Log.logCommandLine("nr of column scans: " + aCount);

		return aMaximallyInformativeItemSet;
	}

	// computes the phi coefficient, which is the Pearson's coefficient between two binary variables.
	public float computeCorrelation(int k, int l)
	{
		long anA = 0;
		long aB = 0;
		long anAB = 0;
		for (int i = 0; i < itsNrRecords ; i++)
		{
			boolean a = itsColumns.get(k).get(i);
			boolean b = itsColumns.get(l).get(i);

			if (a)
				anA++;
			if (b)
				aB++;			
			if (a && b)
				anAB++;
		}

		if (anA*aB*(itsNrRecords - anA)*(itsNrRecords - aB) == 0)
			return 0f;
		else
			return (itsNrRecords*anAB - anA*aB) / (float) Math.sqrt(anA*aB*(itsNrRecords - anA)*(itsNrRecords - aB));
	}

	private final CrossCube countCrossCube()
	{
		CrossCube aCrossCube = new CrossCube(itsColumns.size());
		BitSet aBitSet = new BitSet(itsColumns.size());

		for (int i = 0; i < itsNrRecords ; i++)
		{
			// TODO clear() might not be faster than using new BitSet: profile
			aBitSet.clear();
			for (int j = 0; j < itsColumns.size(); j++)
				aBitSet.set(j, itsColumns.get(j).get(i));

			aCrossCube.incrementCount(aBitSet);
		}

		return aCrossCube;
	}

	public void print()
	{
		int nrColumns = getNrColumns();
		StringBuilder aStringBuilder;

		for (int i = 0, j = getNrRecords(); i < j; i++)
		{
			aStringBuilder = new StringBuilder(nrColumns);
			for (BitSet b : itsColumns)
				aStringBuilder.append(b.get(i) ? "1" : "0");
			Log.logCommandLine(aStringBuilder.toString());
		}
	}

	public int getNrRecords() { return itsNrRecords; }
	public int getNrColumns() { return itsColumns.size(); }
	public void addColumn(BitSet theBitSet) { itsColumns.add(theBitSet); }
	public BitSet getColumn(int theIndex) { return itsColumns.get(theIndex);}
	//public void removeColumn(BitSet theBitSet) {itsColumns.remove(theBitSet);}
	//public void removeColumn(int theIndex) {itsColumns.remove(theIndex);}
	//public void setColumn(BitSet theBitSet, int theIndex) {itsColumns.set(theIndex, theBitSet);}

	////////////////////////////////////////////////////////////////////////////
	// MIKI TEST: NOTE implementation in Miki.java is much faster, use that   //
	////////////////////////////////////////////////////////////////////////////
	private static final boolean MIKI_TEST       = false;
	private static final boolean DEBUG_ROW_PRINT = false;

	// there is a small chance (unlikely) that a long[] with 64 different bit
	// patterns is faster than the shifts used in the row and MIKI loops: test
	private static final double getApproximateMiki(List<BitSet> theColumns, int theNrRecords, int k)
	{
		Log.logCommandLine("finding approximate " + k + "-itemsets");

		// original code is column based, but miki computation is row oriented
		int aNrColumns = theColumns.size();
		int aNrWords   = (aNrColumns >> 6) + 1;
		long[][] aRows = new long[theNrRecords][aNrWords];

		// prepare row array, more convenient for loop below
		for (int i = 0; i < theNrRecords; ++i)
			aRows[i] = new long[aNrWords];

		// stores bits starting at MSB, the reverse of ordinary BitSets
		for (int i = 0; i < aNrColumns; ++i)
		{
			BitSet b = theColumns.get(i);
			long j   = (0x8000_0000_0000_0000L >>> (i & 63));
			for (int l = b.nextSetBit(0), m = (i >> 6); l >= 0; l = b.nextSetBit(l+1))
				aRows[l][m] |= j;
		}

		if (DEBUG_ROW_PRINT)
			for (long[] row : aRows)
				for (int i = 0, j = row.length-1; i <= j; ++i)
					System.out.print(asBinary((i == j ? (aNrColumns & 63) : 64), row[i]) + (i == j ? "\n" : " "));

//		BitSet[] aRowInfo = new BitSet[theNrRecords];
//		for (int i = 0; i < theNrRecords; ++i)
//			aRowInfo[i] = new BitSet(Math.min(aNrColumns, k));
		int[] aRowInfo = new int[theNrRecords];

		// time from here: original set up itsColumn setup during construction
		Timer t = new Timer();

		long aCount            = 0L;
		int[] aBest            = new int[Math.min(Math.min(aNrColumns, k), 30)];
		double aMaximalEntropy = 0.0;

		Arrays.fill(aBest, Integer.MAX_VALUE); // assume aNrColumns < MAX_VALUE

		for (int i = 0, j = 0; i < k; ++i, ++j)   // j=nrSelected
		{
			int aTmp = -1;
		col: for (int l = 0; l < aNrColumns; ++l) // l=column
			{
				for (int m = 0; m < j; ++m)
				{
					int n = aBest[m];
					if (l < n)
						break;
					else if (l == n)
						continue col;
					// else l > n, check next value if available
				}

				++aCount;

				double anEntropy = getEntropy3(aRows, aBest, j, l, aRowInfo, false);
				if (anEntropy > aMaximalEntropy)
				{
					aTmp = l;
					aMaximalEntropy = anEntropy;
					Log.logCommandLine("found a new maximum: " + asBitSet(j, aBest, l) + ": " + aMaximalEntropy);
				}
			}
			if (aTmp == -1)
				break;
			getEntropy3(aRows, aBest, j, aTmp, aRowInfo, true); // reset info for best
			aBest[j] = aTmp;
			Arrays.sort(aBest, 0, j+1);
		}

		Log.logCommandLine("nr of column scans: " + aCount);

		mikiPrint("NEW: " + t.getElapsedTimeString());

		return aMaximalEntropy;
	}

	@SuppressWarnings("unused") // for archival purposes, will be removed
	private static final double getEntropy2(long[][] theRows, int[] theBest, int theNrSelected, int theExtension, BitSet[] theBitSets)
	{
		// CrossCube code cross-over to follow original implementation
		CrossCube aCrossCube = new CrossCube(theNrSelected + 1);

		for (int i = 0, j = theRows.length; i < j; ++i)
		{
			BitSet b = theBitSets[i];
			b.set(theNrSelected, (theRows[i][theExtension >> 6] & (0x8000_0000_0000_0000L >>> (theExtension & 63))) != 0L);
			aCrossCube.incrementCount(b);
		}

		return aCrossCube.getEntropy();
	}

	@SuppressWarnings("unused") // for archival purposes, will be removed
	private static final double getEntropy3(long[][] theRows, int[] theBest, int theNrSelected, int theExtension, BitSet[] theBitSets)
	{
		int[] aCounts = new int[(int) Math.pow(2.0, (theNrSelected+1))];

		for (int i = 0, j = theRows.length; i < j; ++i)
		{
			BitSet b = theBitSets[i];
			b.set(theNrSelected, (theRows[i][theExtension >> 6] & (0x8000_0000_0000_0000L >>> (theExtension & 63))) != 0L);
			long[] la = b.toLongArray();
			++aCounts[la.length == 0 ? 0 : (int) la[0]];
		}

		double aTotalCount = theRows.length;
		double d = 0.0;
		for (int i : aCounts)
		{
			if (i == 0)
				continue;
			double aFraction = (i /  aTotalCount);
			d += (-aFraction * Math.log(aFraction));
		}

		return d / Math.log(2.0);
	}

	// lazy - combines two modes: (prospective) evaluation and data modification
	// will be split when final
	private static final double getEntropy3(long[][] theRows, int[] theBest, int theNrSelected, int theExtension, int[] theRowInfos, boolean modify)
	{
		int[] aCounts = modify ? null : new int[(int) Math.pow(2.0, (theNrSelected+1))];

		for (int i = 0, j = theRows.length, k = (1 << theNrSelected), l = ~k; i < j; ++i)
		{
			int aRowInfo = theRowInfos[i];
			if ((theRows[i][theExtension >> 6] & (0x8000_0000_0000_0000L >>> (theExtension & 63))) != 0L)
				aRowInfo |= k;
			else
				aRowInfo &= l; // no longer needed, data is manipulated once

			if (modify)
				theRowInfos[i] = aRowInfo;
			else
				++aCounts[aRowInfo];
		}
		if (modify)
			return Double.NaN;

		double aTotalCount = theRows.length;
		double d = 0.0;
		for (int i : aCounts)
		{
			if (i == 0)
				continue;
			double aFraction = (i / aTotalCount);
			d += (-aFraction * Math.log(aFraction));
		}

		return d / Math.log(2.0);
	}

	private static final void mikiPrint(String s) { System.out.println(s); }

	// Long.toBinaryString(long) does not print leading zeros
	private static final String asBinary(int aNrColumns, long theLong)
	{
		StringBuilder s = new StringBuilder(aNrColumns);
		for (int i = 63, j = (i-aNrColumns); i > j; --i)
			s.append(((theLong >>> i) & 1L) == 0L ? "0" : "1");
		return s.toString();
	}

	// might copy theColumns, add theExtension, sort, then print
	private static final String asBitSet(int aNrColumns, int[] theColumns, int theExtension)
	{
		StringBuilder s = new StringBuilder(aNrColumns << 3);
		s.append("{");
		for (int i = 0, j = aNrColumns; i < j; ++i)
			s.append(theColumns[i]).append(", ");
		return s.append(theExtension).append("}").toString();
	}
}

package nl.liacs.subdisc;

import java.util.*;

public class RefinementList extends ArrayList<Refinement>
{
	private static final long serialVersionUID = 1L;
	private Table itsTable;
	private Subgroup itsSubgroup;

	public RefinementList(Subgroup theSubgroup, Table theTable, SearchParameters theSearchParameters)
	{
		Log.logCommandLine("refinementlist");

		itsSubgroup = theSubgroup;
		itsTable = theTable;

		final SearchParameters aSP = theSearchParameters;
		final boolean useSets = aSP.getNominalSets();
		final NumericOperatorSetting aNO = aSP.getNumericOperatorSetting();
		final TargetConcept aTC = aSP.getTargetConcept();
		final boolean isSingleNominalTT = (aTC.getTargetType() == TargetType.SINGLE_NOMINAL);

		Condition aCondition = itsTable.getFirstCondition();
		AttributeType aType;
		do
		{
			boolean add = false;
			Column aColumn = aCondition.getColumn();

			if (aColumn.getIsEnabled() && !aTC.isTargetAttribute(aColumn))
			{
				aType = aColumn.getType();
				Refinement aRefinement = new Refinement(aCondition, itsSubgroup);

				// FIXME MM should be part of Operator
				//check validity of operator
				//numeric
				if (aType == AttributeType.NUMERIC && NumericOperatorSetting.check(aNO, aCondition.getOperator()))
					add = true;
				//nominal
				else if (aType == AttributeType.NOMINAL && !useSets && aCondition.isEquals())
				{
					// set-valued only allowed for SINGLE_NOMINAL
					if (isSingleNominalTT || aCondition.getOperator() != Operator.ELEMENT_OF)
						add = true;
				}
				else if (aType == AttributeType.NOMINAL && useSets && aCondition.isElementOf())
					add = true;
				//binary
				else if (aType == AttributeType.BINARY)
					add = true;

				if (add)
				{
					add(aRefinement);
					Log.logCommandLine("   condition: " + aCondition.toString());
				}
			}
		}
		while ((aCondition = itsTable.getNextCondition(aCondition)) != null);
	}
}

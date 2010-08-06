package nl.liacs.subdisc;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.TreeSet;

import nl.liacs.subdisc.Attribute.AttributeType;

public class Column
{
//	private AttributeType itsType; //types in Attribute
	private Attribute itsAttribute;
	private ArrayList<Float> itsFloats;
	private ArrayList<String> itsNominals;
	private BitSet itsBinaries;
	private BitSet itsMissing = new BitSet();
	private int itsSize;
	private float itsMin = Float.POSITIVE_INFINITY;
	private float itsMax = Float.NEGATIVE_INFINITY;
	private boolean isEnabled = true;

	public Column(Attribute theAttribute, int theNrRows)
	{
		itsSize = 0;
		itsAttribute = theAttribute;
		switch(itsAttribute.getType())
		{
			case NUMERIC :
			case ORDINAL : itsFloats = new ArrayList<Float>(theNrRows); break;
			case NOMINAL : itsNominals = new ArrayList<String>(theNrRows); break;
			case BINARY : itsBinaries = new BitSet(theNrRows); break;
			default : itsNominals = new ArrayList<String>(theNrRows); break;	// TODO throw warning
		}
	}

	public void add(float theFloat) { itsFloats.add(new Float(theFloat)); itsSize++; }
	public void add(boolean theBinary)
	{
		if(theBinary)
			itsBinaries.set(itsSize);
		itsSize++;
	}
	public void add(String theNominal) { itsNominals.add(theNominal); itsSize++; }
	public int size() { return itsSize; }
	public Attribute getAttribute() { return itsAttribute; }
	public AttributeType getType() { return itsAttribute.getType(); }
	public String getName() {return itsAttribute.getName(); }
	public float getFloat(int theIndex) { return itsFloats.get(theIndex).floatValue(); }
	public String getNominal(int theIndex) { return itsNominals.get(theIndex); }
	public boolean getBinary(int theIndex) { return itsBinaries.get(theIndex); }
	public String getString(int theIndex)
	{
		switch (itsAttribute.getType())
		{
			case NUMERIC :
			case ORDINAL : return itsFloats.get(theIndex).toString();
			case NOMINAL : return getNominal(theIndex);
			case BINARY : return getBinary(theIndex)?"1":"0";
			default : return ("Unknown type: " + itsAttribute.getTypeName());
		}
	}
	public BitSet getBinaries() { return itsBinaries; }

	public boolean isNominalType() { return itsAttribute.isNominalType(); }
	public boolean isNumericType() { return itsAttribute.isNumericType(); }
	public boolean isOrdinalType() { return itsAttribute.isOrdinalType(); }
	public boolean isBinaryType() { return itsAttribute.isBinaryType(); }

	public float getMin()
	{
		updateMinMax();
		return itsMin;
	}

	public float getMax()
	{
		updateMinMax();
		return itsMax;
	}

	private void updateMinMax()
	{
		if(itsMax == Float.NEGATIVE_INFINITY) //never computed?
			for(int i=0; i<itsSize; i++)
			{
				float aValue = getFloat(i);
				if(aValue > itsMax)
					itsMax = aValue;
				if(aValue < itsMin)
					itsMin = aValue;
			}
	}

	public void print()
	{
		Log.logCommandLine((isEnabled ? "Enabled" : "Disbled") + " Column " + this.getName() + ":"); // TODO TEST ONLY
		switch(itsAttribute.getType())
		{
			case NUMERIC :
			case ORDINAL : Log.logCommandLine(itsFloats.toString()); break;
			case NOMINAL : Log.logCommandLine(itsNominals.toString()); break;
			case BINARY : Log.logCommandLine(itsBinaries.toString()); break;
			default : Log.logCommandLine("Unknown type: " + itsAttribute.getTypeName()); break;
		}
	}

	public TreeSet<String> getDomain()
	{
		TreeSet<String> aResult = new TreeSet<String>();
		if (isBinaryType())
		{
			aResult.add("0");
			aResult.add("1");
			return aResult;
		}

		for (int i=0; i<itsSize; i++)
			if (isNominalType())
				aResult.add(itsNominals.get(i));
			else if (isNumericType())
				aResult.add(Float.toString(itsFloats.get(i)));
			//TODO ordinal?

		return aResult;
	}

	/**
	 * NEW Methods for AttributeType change
	 * TODO update
	 * itsType
	 * (itsTable.)itsAttribute.setType()
	 * itsFloats / itsNominals / itsBinaries
	 * @return 
	 */
	public void setType(String theType) { itsAttribute.setType(theType); }
	public boolean getIsEnabled() { return isEnabled; }
	public void setIsEnabled(boolean theSetting) { isEnabled = theSetting; }
	/**
	 * NOTE use setMissing to set missing values
	 * Editing on the BitSet retrieved through getMissing() has no effect
	 * on the original Columns' itsMissing
	 * @return a clone of this columns itsMissing BitSet
	 */
	public BitSet getMissing() { return (BitSet) itsMissing.clone(); } 
	public void setMissing(int theIndex) { itsMissing.set(theIndex); }
	public boolean isValidValue(String theNewValue)
	{
		switch(getType())
		{
			case NUMERIC :
			case ORDINAL :
			{
				try { Float.parseFloat(theNewValue); return true; }
				catch (NumberFormatException anException) { return false; }
			}
			case NOMINAL : return true;
			case BINARY :
			{
				// TODO use FileLoaderARFF.BOOLEAN_POSITIVE || BOOLEAN_NEGATIVE
				return new ArrayList<String>(Arrays.asList(new String[] { "0", "1", "false", "true", "F", "T", "no", "yes" })).contains(theNewValue);
			}
			default : return false;
		}
	}
	public void setNewMissingValue(String theNewValue)
	{
		for(int i = itsMissing.nextSetBit(0); i >= 0; i = itsMissing.nextSetBit(i + 1))
		{
			switch(getType())
			{
				case NUMERIC :
				case ORDINAL : itsFloats.set(i, Float.valueOf(theNewValue)); break;
				case NOMINAL : itsNominals.set(i, theNewValue); break;
				case BINARY :
				{
					if("0".equalsIgnoreCase(theNewValue))
						itsBinaries.clear(i);
					else
						itsBinaries.set(i);
					break;
				}
			}
//			System.out.println("set: " + i);
		}
	}
}

package nl.liacs.subdisc;

public class Interval
{
	private float itsLower;
	private float itsUpper;

	public Interval(float theLower, float theUpper)
	{
		itsLower = theLower;
		itsUpper = theUpper;
	}

	public boolean between(float theValue)
	{
		return (theValue >= itsLower) && (theValue <= itsUpper);
	}

	public String toString()
	{
		String aLeft = (itsLower == Float.NEGATIVE_INFINITY) ? "<inf" : ("[" + itsLower);
		String aRight = (itsUpper == Float.POSITIVE_INFINITY) ? "inf>" : (Float.toString(itsUpper) + "]");
		return aLeft + ", " + aRight;
	}
}
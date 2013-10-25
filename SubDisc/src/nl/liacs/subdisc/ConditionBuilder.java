package nl.liacs.subdisc;

/*
 * class is implemented as an enum to enforce a single unique instance
 * class offers only static methods, these internally call the sole instance
 * 
 * methods that can be called externally are:
 * build(ConditionBase theConditionBase, String theValue)
 * build(ConditionBase theConditionBase, ValueSet theValue)
 * build(ConditionBase theConditionBase, float theValue)
 * build(ConditionBase theConditionBase, Interval theValue)
 * build(ConditionBase theConditionBase, boolean theValue)
 */
public enum ConditionBuilder
{
	FACTORY;

	/**
	 * Creates a single (String) value {@link ConditionI} for a
	 * {@link AttributeType#NOMINAL} {@link Column}.
	 * 
	 * The Column of the ConditionBase must be of type NOMINAL, and
	 * {@code null}s are not allowed.
	 * 
	 * @param theConditionBase The ConditionBase for this Condition.
	 * @param theValue The value for this Condition.
	 * 
	 * @return A Condition.
	 */
	public static final ConditionI build(ConditionBase theConditionBase, String theValue)
	{
		return FACTORY.new ConditionNominalEquals(theConditionBase, theValue);
	}

	/**
	 * Creates a {@link ValueSet} {@link ConditionI} for a
	 * {@link AttributeType#NOMINAL} {@link Column}
	 * 
	 * The Column of the ConditionBase must be of type NOMINAL, and
	 * {@code null}s are not allowed.
	 * 
	 * @param theConditionBase The ConditionBase for this Condition.
	 * @param theValue The value for this Condition.
	 * 
	 * @return A Condition.
	 */
	public static final ConditionI build(ConditionBase theConditionBase, ValueSet theValue)
	{
		return FACTORY.new ConditionNominalElementOf(theConditionBase, theValue);
	}

	/**
	 * Creates a single (float) value {@link ConditionI} for a
	 * {@link AttributeType#NUMERIC} {@link Column}.
	 * 
	 * The Column of the ConditionBase must be of type NUMERIC,
	 * {@code null} is not allowed for the {ConditionBase}, and
	 * {@code Float.NaN} is not allowed as value.
	 * 
	 * @param theConditionBase The ConditionBase for this Condition.
	 * @param theValue The value for this Condition.
	 * 
	 * @return A Condition.
	 */
	public static final ConditionI build(ConditionBase theConditionBase, float theValue)
	{
		switch (theConditionBase.getOperator())
		{
			case LESS_THAN_OR_EQUAL :
				return FACTORY.new ConditionNumericLeq(theConditionBase, theValue);
			case GREATER_THAN_OR_EQUAL :
				return FACTORY.new ConditionNumericGeq(theConditionBase, theValue);
			case EQUALS :
				return FACTORY.new ConditionNumericEquals(theConditionBase, theValue);
			default :
				throw new AssertionError(theConditionBase.getOperator());
		}
	}

	/**
	 * Creates an Interval {@link ConditionI} for a
	 * {@link AttributeType#NUMERIC} {@link Column}.
	 * 
	 * The Column of the ConditionBase must be of type NUMERIC, and
	 * {@code null}s are not allowed.
	 * 
	 * @param theConditionBase The ConditionBase for this Condition.
	 * @param theValue The value for this Condition.
	 * 
	 * @return A Condition.
	 */
	public static final ConditionI build(ConditionBase theConditionBase, Interval theValue)
	{
		return FACTORY.new ConditionNumericBetween(theConditionBase, theValue);
	}

	/**
	 * Creates a boolean value {@link ConditionI} for a
	 * {@link AttributeType#BINARY} {@link Column}.
	 * 
	 * The Column of the ConditionBase must be of type BINARY, and
	 * {@code null} is not allowed for the {ConditionBase}.
	 * 
	 * @param theConditionBase The ConditionBase for this Condition.
	 * @param theValue The value for this Condition.
	 * 
	 * @return A Condition.
	 */
	public static final ConditionI build(ConditionBase theConditionBase, boolean theValue)
	{
		return FACTORY.new ConditionBinaryEquals(theConditionBase, theValue);
	}

	// see NOTE below
	private static final int UNDETERMINED = Integer.MIN_VALUE;
	// base implementation, compares Column and Operator
	// throws NullPointerException on null arguments
	private static final int compare(ConditionA x, ConditionA y)
	{
		if (x == y)
			return 0;

		// conditions on 'lower' Columns come first
		int cmp = x.itsColumn.getIndex() - y.getColumn().getIndex();
		if (cmp != 0)
			return cmp;

		// about same column, 'lower' Operators come first
		cmp = x.getOperator().ordinal() - y.getOperator().ordinal();
		if (cmp != 0)
			return cmp;

		// about same Columns and Operator, implementation class needs
		// to compare values
		// when Column and Operator match, (throughout an experiment) it
		// is safe to assume the Conditions are of the same class

		// this is a special return value, it lies outside the range of
		// possible return values for the above three tests
		return UNDETERMINED;
	}

	// Condition implementation classes should implement this interface
	interface ConditionI extends Comparable<ConditionA>
	{
		// shared by all Condition implementation classes
		public Column getColumn();

		// implicit for each Condition implementation class
		public Operator getOperator();

		// methods that are shared by all implementations
		// but will throw NoSuchMethodexception unless overridden

		// to be overridden only by ConditionNominalEquals
		public String getNominalValue();

		// to be overridden only by ConditionNominalElementOf
		public ValueSet getNominalValueSet();

		// to be overridden only by ConditionNumericLeq/Geq/Equals
		public float getNumericValue();

		// to be overridden only by ConditionNumericBetween
		public Interval getNumericInterval();

		// to be overridden only by ConditionBinaryEquals
		public boolean getBinaryValue();

		@Override
		public int compareTo(ConditionA theCondition);

		@Override
		public String toString();
	}

	/*
	 * this is the base class that all implementation classes should extend
	 * therefore it is not final
	 */
	private abstract class ConditionA implements ConditionI
	{
		// shared by all Condition implementation classes
		protected final Column itsColumn;

		private ConditionA(ConditionBase theConditionBase, AttributeType theExpectedType, Operator theExpectedOperator)
		{
			if (theConditionBase == null)
				throw new IllegalArgumentException("theConditionBase can not be null");

			// check Column
			Column aColumn = theConditionBase.getColumn();
			if (aColumn.getType() != theExpectedType)
				throw exception("ConditionBase.Column.AttributeType", aColumn.getType().toString());

			Operator anOperator = theConditionBase.getOperator();
			if (anOperator != theExpectedOperator)
				throw exception("ConditionBase.Operator", anOperator.GUI_TEXT);

			itsColumn = aColumn;
		}

		// shared by all Condition implementation classes
		public Column getColumn() { return itsColumn; }

		// implicit for each Condition implementation class
		public Operator getOperator() { throw exception(getClass()); }

		// methods that are shared by all implementations
		// but will throw NoSuchMethodexception unless overridden

		// to be overridden only by ConditionNominalEquals
		public String getNominalValue() { throw exception(getClass()); }

		// to be overridden only by ConditionNominalElementOf
		public ValueSet getNominalValueSet() { throw exception(getClass()); }

		// to be overridden only by ConditionNumericLeq/Geq/Equals
		public float getNumericValue() { throw exception(getClass()); }

		// to be overridden only by ConditionNumericBetween
		public Interval getNumericInterval() { throw exception(getClass()); }

		// to be overridden only by ConditionBinaryEquals
		public boolean getBinaryValue() { throw exception(getClass()); }

		// to be overridden only by all Condition implementations
		public abstract String getValueString();

		// no base implementation, forces subclasses to implement method
		@Override
		public abstract int compareTo(ConditionA theCondition);

		@Override
		public String toString()
		{
			return new StringBuilder(32)
						.append(itsColumn.getName())
						.append(" ")
						.append(getOperator())
						.append(" ")
						.append(getValueString())
						.toString();
		}
	}

	private static final UnsupportedOperationException exception(Class<?> theClass)
	{
		return new UnsupportedOperationException(theClass.getName());
	}

	private static final IllegalArgumentException exception(String pre, String post)
	{
		return new IllegalArgumentException(pre + " can not be " + post);
	}

	// TODO MM theValue.getClass() might return the correct class -> test
	private static final void checkValue(String theValueClass, Object theValue)
	{
		if (theValue == null)
			throw exception(theValueClass, "null");
	}

	private class ConditionNominalEquals extends ConditionA
	{
		private final String itsValue;

		private ConditionNominalEquals(ConditionBase theConditionBase, String theValue)
		{
			// assume that ConditionBase checked validity of Column-Operator
			// String can only be used with EQUALS
			super(theConditionBase, AttributeType.NOMINAL, Operator.EQUALS);
			checkValue(String.class.getSimpleName(), theValue);
			itsValue = theValue;
		}

		@Override
		public final Operator getOperator() { return Operator.EQUALS; }

		@Override
		public final String getNominalValue() { return itsValue; }

		@Override
		public final String getValueString() { return "'" + itsValue + "'"; }

		@Override
		public int compareTo(ConditionA theCondition)
		{
			int cmp = compare(this, theCondition);
			if (cmp != UNDETERMINED)
				return cmp;
			// safe enough, when Column and Operator match
			ConditionNominalEquals that = ((ConditionNominalEquals) theCondition);
			return this.itsValue.compareTo(that.itsValue);
		}
	}

	private class ConditionNominalElementOf extends ConditionA
	{
		private final ValueSet itsValue;

		private ConditionNominalElementOf(ConditionBase theConditionBase, ValueSet theValue)
		{
			// assume that ConditionBase checked validity of Column-Operator
			// ValueSet can only be used with ELEMENt_OF
			super(theConditionBase, AttributeType.NOMINAL, Operator.ELEMENT_OF);
			checkValue(ValueSet.class.getSimpleName(), theValue);
			itsValue = theValue;
		}

		@Override
		public final Operator getOperator() { return Operator.ELEMENT_OF; }

		@Override
		public final ValueSet getNominalValueSet() { return itsValue; }

		@Override
		public final String getValueString() { return itsValue.toString(); }

		@Override
		public int compareTo(ConditionA theCondition)
		{
			int cmp = compare(this, theCondition);
			if (cmp != UNDETERMINED)
				return cmp;
			assert (uniqueValueSet(this, theCondition));
			return 0;
		}
	}

	private static final boolean uniqueValueSet(ConditionNominalElementOf x, ConditionA y)
	{
		// safe enough, when Column and Operator match
		if (x.itsValue != ((ConditionNominalElementOf) y).itsValue)
		{
			Log.logCommandLine(String.format("Multiple %ss for %s '%s'",
					x.itsValue.getClass().getSimpleName(),
					x.itsColumn.getClass().getSimpleName(),
					x.itsColumn.getName()));
			return false;
		}
		return true;
	}

	private abstract class ConditionNumericValue extends ConditionA
	{
		private final float itsValue;

		private ConditionNumericValue(ConditionBase theConditionBase, Operator theExpectedOperator, float theValue)
		{
			super(theConditionBase, AttributeType.NUMERIC, theExpectedOperator);
			// check value
			if (Float.isNaN(theValue))
				throw exception("float", "NaN");
			itsValue = theValue;
		}

		@Override
		public abstract Operator getOperator();

		@Override
		public final float getNumericValue() { return itsValue; }

		@Override
		public final String getValueString() { return Float.toString(itsValue); }

		@Override
		public int compareTo(ConditionA theCondition)
		{
			int cmp = compare(this, theCondition);
			if (cmp != UNDETERMINED)
				return cmp;
			// safe enough, when Column and Operator match
			ConditionNumericValue that = ((ConditionNumericValue) theCondition);
			// NOTE considers 0.0 to be greater than -0.0
			return Float.compare(this.itsValue, that.itsValue);
		}
	}

	private class ConditionNumericLeq extends ConditionNumericValue
	{
		private ConditionNumericLeq(ConditionBase theConditionBase, float theValue)
		{
			super(theConditionBase, Operator.LESS_THAN_OR_EQUAL, theValue);
		}

		@Override
		public final Operator getOperator() { return Operator.LESS_THAN_OR_EQUAL; }
	}

	private class ConditionNumericGeq extends ConditionNumericValue
	{
		private ConditionNumericGeq(ConditionBase theConditionBase, float theValue)
		{
			super(theConditionBase, Operator.GREATER_THAN_OR_EQUAL, theValue);
		}

		@Override
		public final Operator getOperator() { return Operator.GREATER_THAN_OR_EQUAL; }
	}

	private class ConditionNumericEquals extends ConditionNumericValue
	{
		private ConditionNumericEquals(ConditionBase theConditionBase, float theValue)
		{
			super(theConditionBase, Operator.EQUALS, theValue);
		}

		@Override
		public final Operator getOperator() { return Operator.EQUALS; }
	}

	private class ConditionNumericBetween extends ConditionA
	{
		private final Interval itsValue;

		private ConditionNumericBetween(ConditionBase theConditionBase, Interval theValue)
		{
			// assume that ConditionBase checked validity of Column-Operator
			// Interval can only be used with BETWEEN
			super(theConditionBase, AttributeType.NUMERIC, Operator.BETWEEN);
			checkValue(Interval.class.getSimpleName(), theValue);
			itsValue = theValue;
		}

		@Override
		public final Operator getOperator() { return Operator.BETWEEN; }

		@Override
		public final Interval getNumericInterval() { return itsValue; }

		@Override
		public final String getValueString() { return itsValue.toString(); }

		@Override
		public int compareTo(ConditionA theCondition)
		{
			int cmp = compare(this, theCondition);
			if (cmp != UNDETERMINED)
				return cmp;
			// safe enough, when Column and Operator match
			ConditionNumericBetween that = ((ConditionNumericBetween) theCondition);
			return Float.compare(this.itsValue, that.itsValue);
		}
	}

	// the comparison and toString() for this class assume the following:
	static { assert (SubgroupDiscovery.equalsIsOnlyBinaryOperator()); }
	private class ConditionBinaryEquals extends ConditionA
	{
		private final boolean itsValue;

		private ConditionBinaryEquals(ConditionBase theConditionBase, boolean theValue)
		{
			// assume that ConditionBase checked validity of Column-Operator
			// there is only one possibility
			super(theConditionBase, AttributeType.BINARY, Operator.EQUALS);
			itsValue = theValue;
		}

		@Override
		public final Operator getOperator() { return Operator.EQUALS; }

		@Override
		public final boolean getBinaryValue() { return itsValue; }

		@Override
		public final String getValueString()
		{
			return itsValue ?
				AttributeType.DEFAULT_BINARY_TRUE_STRING :
				AttributeType.DEFAULT_BINARY_FALSE_STRING;
		}

		@Override
		public int compareTo(ConditionI theCondition)
		{
			throw exception(getClass());
		}
	}
}

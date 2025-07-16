package ru.biosoft.lims.pathocalc;

public class ACMGCriteriaValue {
	public ACMGCriteria criteria;
	
	public Object value;///????
	
	public enum State {MET, UNMET, UNDEFINED};
	public State state;

	public String comment;
	
	public Object rule;///????
	
	public ACMGCriteriaValue(ACMGCriteria criteria, State state)
	{
		this.criteria = criteria;
		this.state = state;
	}
}

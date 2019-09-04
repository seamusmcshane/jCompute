package jcompute.results.custom;

public interface CustomItemResultInf
{
	/**
	 * A filename assocated with theses results.
	 * 
	 * @return The log file name.
	 */
	public String getLogFileName();
	
	/**
	 * Total number of Fields
	 * 
	 * @return Total field count.
	 */
	public int getTotalFields();
	
	/**
	 * Get a single fields name/heading.
	 * 
	 * @param index index of the heading you wish to get.
	 * @return The heading at the index.
	 */
	public String getFieldHeading(int index);
	
	/**
	 * Gets a single fields value.
	 * 
	 * @param index index of the value you wish to get.
	 * @return The valut at the index.
	 */
	public Object getFieldValue(int index);
	
	/**
	 * Returns the field type as a CustomResultFieldType 
	 * 
	 * @param index index of the field type you wish to get
	 * @return CustomResultFieldType indicating the field type.
	 * @see jcompute.results.custom.CustomResultFieldType
	 */
	public CustomResultFieldType getFieldType(int index);
	
	/**
	 * Sets the vault of a field.
	 * @param index Index of the field.
	 * @param value The vault to be set.
	 */
	public void setFieldValue(int index, Object value);
}

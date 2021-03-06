package com.eroi.migrate.schema;

public class Column {

	private String columnName;
	private int columnType;
	private int length;
	private Integer precision;
	private Integer scale;
	private boolean primaryKey;
	private boolean nullable;
	private Object defaultValue;
	private boolean autoincrement;
	private boolean unicode;
	private String columnComment;// 字段注释

	public Column(String columnName, int columnType) {
		this(columnName, columnType, -1, false, true, null, false);
	}

	public Column(String columnName, int columnType, int length, boolean primaryKey, boolean nullable, Object defaultValue, boolean autoincrement) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.length = length;
		this.primaryKey = primaryKey;
		this.nullable = !primaryKey && nullable;
		this.defaultValue = defaultValue;
		this.autoincrement = autoincrement;

		unicode = false;
		precision = null;
		scale = null;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getColumnType() {
		return columnType;
	}

	public int getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
		setNullable(false);
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isAutoincrement() {
		return autoincrement;
	}

	public void setAutoIncrement(boolean autoincrement) {
		this.autoincrement = autoincrement;
	}

	public boolean isUnicode() {
		return unicode;
	}

	public void setUnicode(boolean unicode) {
		this.unicode = unicode;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public String getColumnComment() {
		return columnComment;
	}

	public void setColumnComment(String columnComment) {
		this.columnComment = columnComment;
	}

}

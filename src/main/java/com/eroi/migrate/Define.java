package com.eroi.migrate;

import java.sql.Types;

import com.eroi.migrate.schema.CascadeRule;
import com.eroi.migrate.schema.Column;
import com.eroi.migrate.schema.ForeignKey;
import com.eroi.migrate.schema.Index;
import com.eroi.migrate.schema.Table;

/**
 * Creates new schema element objects that can be
 * added to a schema with the Execute class
 *
 */
public class Define {

	/**
	 * Represents a columns data type
	 *
	 */
	public enum DataTypes {
		BIT(Types.BIT), 
		TINYINT(Types.TINYINT), SMALLINT(Types.SMALLINT), INTEGER(Types.INTEGER), BIGINT(Types.BIGINT), FLOAT(Types.FLOAT), REAL(
				Types.REAL), DOUBLE(Types.DOUBLE), NUMERIC(Types.NUMERIC), DECIMAL(Types.DECIMAL), CHAR(Types.CHAR), VARCHAR(Types.VARCHAR), LONGVARCHAR(
				Types.LONGVARCHAR), DATE(Types.DATE), TIME(Types.TIME),
				DATETIME(94),TIMESTAMP(Types.TIMESTAMP), BINARY(Types.BINARY), VARBINARY(Types.VARBINARY), LONGVARBINARY(
				Types.LONGVARBINARY), BLOB(Types.BLOB), CLOB(Types.CLOB), BOOLEAN(Types.BOOLEAN);

		private int typeValue;

		private DataTypes(int typeValue) {
			this.typeValue = typeValue;
		}

		public int getTypeValue() {
			return this.typeValue;
		}

	}

	/**
	 * Represents a column to be added to a schema
	 * 
	 * @param columnName
	 * @param columnType
	 * @param columnOption optionally pass any number of ColumnOptions
	 */
	public static Column column(String columnName, DataTypes columnType, ColumnOption<?>... columnOption) {
		Column column = new Column(columnName, columnType.getTypeValue());

		if (columnOption != null && columnOption.length > 0) {
			for (ColumnOption<?> option : columnOption) {
				if (option != null) {
					option.decorate(column);
				}
			}
		}

		return column;
	}

	public static Column column(String columnName, String columnComment, DataTypes columnType, ColumnOption<?>... columnOption) {
		Column column = new Column(columnName, columnType.getTypeValue());
		column.setColumnComment(columnComment);
		if (columnOption != null && columnOption.length > 0) {
			for (ColumnOption<?> option : columnOption) {
				if (option != null) {
					option.decorate(column);
				}
			}
		}

		return column;
	}

	/**
	 * Represents a table to be added to a schema.
	 * 
	 * @param tableName
	 * @param columns
	 * @return
	 */
	public static Table table(String tableName, Column... columns) {
		return new Table(tableName, columns);
	}

	public static Table table(String tableName, String tableComment, Column... columns) {
		return new Table(tableName, tableComment, columns);
	}

	/**
	 * Represents an index to be added to a schema
	 * 
	 * @param indexName
	 * @param tableName
	 * @param columnNames
	 * @return
	 */
	public static Index index(String indexName, String tableName, String... columnNames) {
		return new Index(indexName, tableName, columnNames, false, false);
	}

	/**
	 * Represents a unique index to be added to a schema
	 * 
	 * @param indexName
	 * @param tableName
	 * @param columnNames
	 * @return
	 */
	public static Index uniqueIndex(String indexName, String tableName, String... columnNames) {
		return new Index(indexName, tableName, columnNames, true, false);
	}

	/**
	 * Represents a foreign key to be added to a schema. Parent
	 * table/column are the table which contains the primary key
	 * that the child table/column refer to.
	 * 
	 * @param name
	 * @param parentTable
	 * @param parentColumn
	 * @param childTable
	 * @param childColumn
	 * @return
	 */
	public static ForeignKey foreignKey(String name, String parentTable, String parentColumn, String childTable, String childColumn) {
		return new ForeignKey(name, parentTable, parentColumn, childTable, childColumn);
	}

	/**
	 * Represents a foreign key to be added to a schema with delete
	 * and update cascade rules
	 * 
	 * @param name
	 * @param parentTable
	 * @param parentColumn
	 * @param childTable
	 * @param childColumn
	 * @param deleteRule
	 * @param updateRule
	 * @return
	 */
	public static ForeignKey foreignKey(String name, String parentTable, String parentColumn, String childTable, String childColumn,
			CascadeRule deleteRule, CascadeRule updateRule) {
		return new ForeignKey(name, parentTable, parentColumn, childTable, childColumn, deleteRule, updateRule);
	}

	/**
	 * Represents properties of a column
	 *
	 * @param <T>
	 */
	public interface ColumnOption<T> {
		public void decorate(Column column);
	}

	/**
	 * Add to columns to indicate whether column is nullable
	 *
	 */
	public static class NotNull implements ColumnOption<Boolean> {
		private Boolean notNull;

		public NotNull(Boolean notnull) {
			notNull = notnull;
		}

		public NotNull() {
			this(true);
		}

		public void decorate(Column column) {
			column.setNullable(!notNull);
		}
	}

	/**
	 * Allows specifying whether column accepts null
	 * 
	 * @param notnull
	 */
	public static NotNull notnull(Boolean notnull) {
		return new NotNull(notnull);
	}

	/**
	 * Indicates column accepts null values;
	 * 
	 */
	public static NotNull notnull() {
		return notnull(true);
	}

	/**
	 * Indicates the column automatically increments.
	 *
	 */
	public static class AutoIncrement implements ColumnOption<Boolean> {
		private Boolean autoincrement;

		public AutoIncrement(Boolean isAutoincrement) {
			autoincrement = isAutoincrement;
		}

		public AutoIncrement() {
			this(true);
		}

		public void decorate(Column column) {
			column.setAutoIncrement(autoincrement);
		}
	}

	/**
	 * Allow specifying whether column is auto incrementing
	 * 
	 * @param isAutoincrement
	 * @return
	 */
	public static AutoIncrement autoincrement(Boolean isAutoincrement) {
		return new AutoIncrement(isAutoincrement);
	}

	/**
	 * Indicates column is auto incrementing
	 * 
	 * @return
	 */
	public static AutoIncrement autoincrement() {
		return autoincrement(true);
	}

	/**
	 * Specifys a column as unicode (for example, in many
	 * database products, turns a VARCHAR into an NVARCHAR)
	 *
	 */
	public static class Unicode implements ColumnOption<Boolean> {
		private Boolean isUnicode;

		public Unicode(Boolean unicode) {
			isUnicode = unicode;
		}

		public Unicode() {
			this(true);
		}

		public void decorate(Column column) {
			column.setUnicode(isUnicode);
		}
	}

	/**
	 * Indicates whether column should support unicode
	 * 
	 * @param isUnicode
	 * @return
	 */
	public static Unicode unicode(Boolean isUnicode) {
		return new Unicode(isUnicode);
	}

	/**
	 * Indicates column should support unicode
	 * 
	 * @return
	 */
	public static Unicode unicode() {
		return unicode(true);
	}

	/**
	 * Indicates column is a primary key
	 *
	 */
	public static class PrimaryKey implements ColumnOption<Boolean> {
		private Boolean isPrimaryKey;

		public PrimaryKey(Boolean primarykey) {
			isPrimaryKey = primarykey;
		}

		public PrimaryKey() {
			this(true);
		}

		public void decorate(Column column) {
			column.setPrimaryKey(isPrimaryKey);
		}
	}

	/**
	 * Allows specifying whether column is a primary key
	 * 
	 * @param isPrimary
	 * @return
	 */
	public static PrimaryKey primarykey(Boolean isPrimary) {
		return new PrimaryKey(isPrimary);
	}

	/**
	 * Indicates a column is part of a primary key
	 */
	public static PrimaryKey primarykey() {
		return primarykey(true);
	}

	/**
	 * Allows appending a length to a data type (such as
	 * VARCHAR(50))
	 *
	 */
	public static class Length implements ColumnOption<Integer> {
		private Integer myLength;

		public Length(Integer myLen) {
			myLength = myLen;
		}

		public void decorate(Column column) {
			column.setLength(myLength);
		}

	}

	/**
	 * Allow specifying the length of a column
	 * 
	 * @param len
	 * @return
	 */
	public static Length length(Integer len) {
		return new Length(len);
	}

	public static class Precision implements ColumnOption<Integer> {
		private Integer precision;

		public Precision(Integer precision) {
			this.precision = precision;
		}

		public void decorate(Column column) {
			column.setPrecision(precision);
		}
	}

	public static Precision precision(Integer precision) {
		return new Precision(precision);
	}

	public static class Scale implements ColumnOption<Integer> {
		private Integer scale;

		public Scale(Integer scale) {
			this.scale = scale;
		}

		public void decorate(Column column) {
			column.setScale(scale);
		}
	}

	public static Scale scale(Integer scale) {
		return new Scale(scale);
	}

	/**
	 * Allows specifying a default value for a column
	 *
	 */
	public static class DefaultValue implements ColumnOption<Object> {
		private Object defaultObject;

		public DefaultValue(Object obj) {
			defaultObject = obj;
		}

		public void decorate(Column column) {
			column.setDefaultValue(defaultObject);
		}
	}

	/**
	 * Allows specifying a default value for a column
	 * 
	 * @param obj
	 * @return
	 */
	public static DefaultValue defaultValue(Object obj) {
		return new DefaultValue(obj);
	}

}

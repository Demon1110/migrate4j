package com.eroi.migrate.schema;

import com.eroi.migrate.misc.Validator;

public class Table {

	private String tableName;
	private String tableComment;// 表注释
	private Column[] columns;

	public Table(String tableName, Column[] columns) {

		Validator.notNull(tableName, "String tableName cannot be null");
		Validator.notNull(columns, "Columns can not be null");
		Validator.isTrue(columns.length > 0, "Must include at least one column");

		this.tableName = tableName;
		this.columns = columns;
	}

	public Table(String tableName, String tableComment, Column[] columns) {
		this(tableName, columns);
		this.tableComment = tableComment;
	}

	public String getTableName() {
		return tableName;
	}

	public Column[] getColumns() {
		return columns;
	}

	public String getTableComment() {
		return tableComment;
	}

}

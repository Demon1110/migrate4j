package com.eroi.migrate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eroi.migrate.misc.Closer;
import com.eroi.migrate.misc.Log;
import com.eroi.migrate.misc.SchemaMigrationException;
import com.eroi.migrate.version.VersionMigrator;
import com.eroi.migrate.version.VersionQuery;

/**
 * Applies or rolls back migration classes
 *
 */
public class Engine {

	private static final Log log = Log.getLog(Engine.class);

	private final ConfigStore config;

	private ConfigStore saveDefaultConfiguration;

	private Engine(ConfigStore cfg) {
		this.config = cfg;
	}

	/**
	 * Applies all migrations that have not been applied.
	 * This method uses the default configuration define in {@link Configure}
	 */
	public static void migrate() {
		migrate(Integer.MAX_VALUE);
	}

	public static void db2Bean(String dbName) {

		Connection conn = Configure.getConnection();
		if (dbName == null) {
			dbName = Configure.getDefaultConfiguration().getDatabase();
		}
		String sql = "SELECT TABLE_NAME,table_comment FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + dbName + "'";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String table = rs.getString(1);
				String comment = rs.getString(2);
				table2Bean(table, comment, dbName, conn);
			}
			Closer.close(ps);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			Closer.close(conn);
		}
		System.out.println("Done");
	}

	public static void table2Bean(String table, String table_comment, String databaseName, Connection conn) {
		if (conn == null) {
			conn = Configure.getConnection();
		}
		if (databaseName == null) {
			databaseName = Configure.getDefaultConfiguration().getDatabase();
		}
		if (table_comment == null) {
			table_comment = "xxxx表";
		}
		String sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE,IS_NULLABLE,CHARACTER_MAXIMUM_LENGTH,COLUMN_KEY, COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '"
				+ databaseName + "' and TABLE_NAME='" + table + "'";
		try {
			String prefixString = Configure.getDefaultConfiguration().getBaseClassPath();
			int i = getMaxNum(prefixString);
			File file = new File(prefixString + i + ".java");
			String head = "package db.migrations.db;\r\nimport com.eroi.migrate.AbstractMigration;\r\npublic class "
					+ Configure.getDefaultConfiguration().getClassprefix() + Configure.getDefaultConfiguration().getSeparator() + i
					+ " extends AbstractMigration {\r\n	private String tableName = \"" + table
					+ "\";\r\n\r\n	public void up() {\r\n		if (!tableExists(this.tableName)) {\r\n			createTable(table(this.tableName, \""
					+ table_comment + "\",\r\n";
			appendString(head, file, "utf-8", false);// 添加头
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			StringBuffer content = new StringBuffer();
			while (rs.next()) {
				String colName = rs.getString(2);
				String dataType = rs.getString(3);
				String isNullable = rs.getString(4);
				String length = rs.getString(5);
				String priKey = rs.getString(6);
				String comment = rs.getString(7);
				content.append("					column(\"").append(colName).append("\", \"").append(comment).append("\", ");
				if ("int".equals(dataType)) {
					content.append("INTEGER");
				}
				else if ("text".equalsIgnoreCase(dataType)) {
					content.append("VARCHAR");
				}
				else if ("longtext".equalsIgnoreCase(dataType)) {
					content.append("VARCHAR");
				}
				else {
					content.append(dataType.toUpperCase());
				}
				if (length != null && !"null".equals(length)) {
					content.append(", length(").append(length).append(")");
				}
				if (priKey != null && "PRI".equals(priKey)) {
					content.append(", primarykey() ");
				}
				if (isNullable != null && "NO".equals(isNullable)) {
					content.append(", notnull() ");
				}
				content.append("),\r\n");
			}
			if (content.length() > 10) {
				content.delete(content.length() - 3, content.length());
			}
			content.append("));");
			appendString(content.toString(), file, "utf-8", true);
			String tail = "\r\n       }\r\n	}\r\n\r\n	public void down() {\r\n		dropTable(this.tableName);\r\n	}\r\n}";
			appendString(tail, file, "utf-8", true);// 添加尾
			Closer.close(ps);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static int getMaxNum(String prefix) {
		int i = Configure.getDefaultConfiguration().getStartIndex();
		File file = new File(prefix + i + ".java");
		System.out.println(file.getAbsolutePath());
		while (exist(file)) {
			i++;
			file = new File(prefix + i + ".java");
			Configure.getDefaultConfiguration().setStartIndex(i);
		}
		return i;
	}

	public static File appendString(String content, File file, String charsetName, boolean isAppend) {

		BufferedWriter writer = null;
		try {
			Charset charSet = charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(touch(file), isAppend), charSet));
			writer.write(content);
			writer.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	public static File touch(File file) {
		if (null == file) {
			return null;
		}
		if (false == file.exists()) {
			mkParentDirs(file);
			try {
				file.createNewFile();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static File mkParentDirs(File file) {
		final File parentFile = file.getParentFile();
		if (null != parentFile && false == parentFile.exists()) {
			parentFile.mkdirs();
		}
		return parentFile;
	}

	public static boolean exist(File file) {
		return (file == null) ? false : file.exists();
	}

	/**
	 * Applies (or rolls back) migrations such that
	 * all migrations up and and including <code>version</code> are in the schema.
	 * This method uses the default configuration define in {@link Configure}
	 */
	public static void migrate(int version) {
		new Engine(Configure.getDefaultConfiguration())._migrateTo(version);
	}

	/**
	 * Applies all migrations that have not been applied using the given configuration
	 */
	public static void migrate(ConfigStore cfg) {
		migrate(cfg, Integer.MAX_VALUE);
	}

	/**
	 * Applies (or rolls back) migrations using the given configuration such that
	 * all migrations up and and including <code>version</code> are in the schema.
	 */
	public static void migrate(ConfigStore cfg, int version) {
		Engine engine = new Engine(cfg);
		try {
			// during this migration holds: cfg == Configure.getDefaultConfiguration()
			engine.saveDefaultConfiguration = Configure.getDefaultConfiguration();
			Configure.setDefaultConfiguration(cfg);
			engine._migrateTo(version);
		}
		finally {
			Configure.setDefaultConfiguration(engine.saveDefaultConfiguration);
		}
	}

	/**
	 * Applies (or rolls back) migrations such that
	 * all migrations up and and including <code>version</code> are in the schema.
	 */
	private void _migrateTo(int version) {

		log.debug("Migrating to version " + version);

		List<Class<? extends Migration>> classesToMigrate = _classesToMigrate();
		if (classesToMigrate == null || classesToMigrate.size() <= 0) {
			log.debug("No migration classes match " + this.config.getBaseClassName());
			return;
		}

		int currentVersion = -1;

		try {
			Connection connection = this.config.getConnection();
			currentVersion = _getCurrentVersion(connection);

			log.debug("Current version is " + currentVersion);
		}
		catch (SQLException e) {
			log.error("Failed to get current version from the database", e);
			throw new SchemaMigrationException("Failed to get current version from the database", e);
		}

		if (currentVersion == version) {
			// nothing to do
			return;
		}

		boolean isUp = _isUpMigration(currentVersion, version);
		// List<Class<? extends Migration>> classesToMigrate = classesToMigrate();
		classesToMigrate = _orderMigrations(classesToMigrate, currentVersion, version);

		int lastVersion = currentVersion;
		Exception exception = null;

		for (int x = 0; x < classesToMigrate.size(); x++) {
			// Execute each migration

			try {
				Class<? extends Migration> migrationClass = classesToMigrate.get(x);

				if (log.isDebugEnabled()) {
					String direction = isUp ? "Running " : "Rolling back";
					log.debug(direction + " migration " + migrationClass.getName());
				}

				lastVersion = _runMigration(migrationClass, isUp);
			}
			catch (Exception e) {
				exception = e;
				break;
			}
		}

		if (lastVersion != currentVersion) {
			try {
				_updateCurrentVersion(lastVersion);

			}
			catch (SQLException e) {
				log.error("Failed to update " + this.config.getVersionTable() + " with version " + lastVersion, e);
				throw new SchemaMigrationException("Failed to update " + this.config.getVersionTable() + " with version " + lastVersion, e);
			}
		}

		if (exception != null) {
			log.error("Migration failed", exception);
			throw new SchemaMigrationException("Migration failed", exception);
		}

		log.debug("Migration complete");
	}

	private int _runMigration(Class<? extends Migration> classToMigrate, boolean isUp) {
		int retVal = _getVersionNumber(classToMigrate.getName());

		if (retVal < 0) {
			// Theoretically, this can't happen
			throw new SchemaMigrationException("Invalid classname " + classToMigrate.getName() + ".");
		}

		try {
			Migration migration = (Migration) classToMigrate.newInstance();
			if (migration instanceof AbstractMigration) {
				((AbstractMigration) migration).setConfigStore(this.config);
				((AbstractMigration) migration).init();
			}

			if (isUp) {
				migration.up();
			}
			else {
				migration.down();
				retVal--; // Just removed this version
			}

			return retVal;
		}
		catch (InstantiationException e) {
			log.error("Instantiation Exception Occured in Engine.runMigration", e);
			throw new SchemaMigrationException(e);
		}
		catch (IllegalAccessException e) {
			log.error("IllegalAccessException Occoured in Engine.runMigration", e);
			throw new SchemaMigrationException(e);
		}
	}

	private int _getCurrentVersion(Connection connection) throws SQLException {

		int result;

		// migrate Version table
		_migrateVersionTable(connection);

		result = VersionQuery.getVersion(this.config);

		return result;
	}

	private void _migrateVersionTable(Connection connection) throws SQLException {

		synchronized (VersionMigrator.INSTANCE) {
			if (VersionMigrator.INSTANCE.isRunning())
				return;

			try {
				VersionMigrator.INSTANCE.runMigration(this.config);
			}
			finally {
				VersionMigrator.INSTANCE.endMigration();
			}
		}
	}

	private void _updateCurrentVersion(int lastVersion) throws SQLException {

		VersionQuery.updateVersion(this.config, lastVersion);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Class<? extends Migration>> _classesToMigrate() {

		List<Class<? extends Migration>> retVal = new ArrayList<Class<? extends Migration>>();
		String baseName = this.config.getBaseClassName();

		int item = this.config.getStartIndex().intValue();

		// help migrate4j to load foreign classes
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = Engine.class.getClassLoader();
		}

		while (true) {
			String classname = baseName + item;

			log.debug("Looking for classname " + classname);

			try {

				Class clazz = Class.forName(classname, false, loader);
				retVal.add(clazz);

				log.debug("Found classname " + classname);
			}
			catch (Exception e) {
				log.debug("Assuming there are no files including or beyond " + classname);
				break;
			}
			item++;
		}

		return retVal;
	}

	private List<Class<? extends Migration>> _orderMigrations(List<Class<? extends Migration>> migrationClasses, int currentVersion, int targetVersion) {

		List<Class<? extends Migration>> retVal = new ArrayList<Class<? extends Migration>>();
		String baseName = this.config.getBaseClassName();
		boolean goUp = true;

		String startClass = baseName + (currentVersion + 1);
		String endClass = baseName + targetVersion;

		if (!_isUpMigration(currentVersion, targetVersion)) {

			// Going down
			startClass = baseName + (targetVersion + 1);
			endClass = baseName + currentVersion;
			goUp = false;

		}

		boolean hasStarted = false;

		for (Class<? extends Migration> clazz : migrationClasses) {

			if (!hasStarted) {

				if (clazz.getName().equals(startClass)) {
					// Just set this to true - the class
					// will be collected in the next "if"
					hasStarted = true;
				}
			}

			if (hasStarted) {

				int index = goUp ? retVal.size() : 0;
				retVal.add(index, clazz);

				if (clazz.getName().equals(endClass)) {
					// We've already got the class, so
					// just get out
					break;
				}
			}
		}

		return retVal;
	}

	private int _getVersionNumber(String classname) {
		int retVal = -1;

		String baseName = this.config.getBaseClassName();

		if (classname.startsWith(baseName)) {
			String id = classname.substring(baseName.length());

			try {
				return Integer.parseInt(id);
			}
			catch (NumberFormatException e) {
				log.error("Invalid classname - can't determine version from " + classname, e);
			}
		}

		return retVal;
	}

	private boolean _isUpMigration(int currentVersion, int targetVersion) {
		return currentVersion < targetVersion;
	}

	// ----------------------------------------------------------------------------------------------------
	// for access from junit test cases
	// ----------------------------------------------------------------------------------------------------

	private static Engine getDefaultEngine() {
		return new Engine(Configure.getDefaultConfiguration());
	}

	public static int getCurrentVersion(Connection connection) throws SQLException {
		return getDefaultEngine()._getCurrentVersion(connection);
	}

	protected static List<Class<? extends Migration>> classesToMigrate() {
		return getDefaultEngine()._classesToMigrate();
	}

	protected static List<Class<? extends Migration>> orderMigrations(List<Class<? extends Migration>> migrationClasses, int currentVersion,
			int targetVersion) {
		return getDefaultEngine()._orderMigrations(migrationClasses, currentVersion, targetVersion);
	}

	protected static int getVersionNumber(String classname) {
		return getDefaultEngine()._getVersionNumber(classname);
	}

	public static void main(String[] args) {

		Configure.configure();

		Integer version = null;
		if (args.length > 0) {
			try {
				version = new Integer(args[0]);
			}
			catch (NumberFormatException e) {
				System.out.println(args[0] + " is not a valid version number");
				throw new RuntimeException(e);
			}
		}

		if (version == null) {
			Engine.migrate();
		}
		else {
			Engine.migrate(version.intValue());
		}

		System.out.println("Done");
	}
}

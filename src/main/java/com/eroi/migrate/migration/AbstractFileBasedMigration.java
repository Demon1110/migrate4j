package com.eroi.migrate.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import com.eroi.migrate.AbstractMigration;
import com.eroi.migrate.ScriptRunner;
import com.eroi.migrate.misc.Log;

/**
 * Applies generic SQL scripts. Script paths
 * are passed into the constructor and must
 * be loadable at runtime.
 * 
 * Because the scripts contain DDL, they will
 * likely be platform specific!
 * 
 *
 */
public abstract class AbstractFileBasedMigration extends AbstractMigration {

	private static Log log = Log.getLog(AbstractFileBasedMigration.class);

	private String upScriptPath;
	private String downScriptPath;
	private String description;

	@Override
	public void up() {
		Connection conn = getConfiguration().getConnection();
		Reader reader = getReader(upScriptPath);
		ScriptRunner runner = new ScriptRunner(conn);
		runner.runScript(reader);
		try {
			conn.setAutoCommit(true);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void down() {
		Connection conn = getConfiguration().getConnection();
		Reader reader = getReader(downScriptPath);
		ScriptRunner runner = new ScriptRunner(conn);
		runner.runScript(reader);
		try {
			conn.setAutoCommit(true);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Reader getReader(String scriptPath) {
		if (scriptPath == null) {
			return null;
		}
		URL url = AbstractFileBasedMigration.class.getClassLoader().getResource(scriptPath);
		File file = new File(url.getFile());

		if (!file.exists()) {
			log.warn(scriptPath + " does not exist - no migration work is being done");
			return null;
		}
		BufferedReader buffer = null;
		try {
			buffer = new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			log.warn(scriptPath + " could not be loaded - no migration work is being done");
			return null;
		}
		return buffer;
	}

	abstract public void initPath();

	@Override
	protected void init() {
		this.initPath();
	}

	public String getDescription() {
		return description;
	}

	public String getUpScriptPath() {
		return upScriptPath;
	}

	public void setUpScriptPath(String upScriptPath) {
		this.upScriptPath = upScriptPath;
	}

	public String getDownScriptPath() {
		return downScriptPath;
	}

	public void setDownScriptPath(String downScriptPath) {
		this.downScriptPath = downScriptPath;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

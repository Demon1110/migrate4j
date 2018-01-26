package db.migrations.project_x;

import com.eroi.migrate.AbstractMigration;
import com.eroi.migrate.Define;
import com.eroi.migrate.generators.MySQLGenerator;
import com.eroi.migrate.misc.Log;

public class Migration_1 extends AbstractMigration {

	@Override
	public void down() {
		dropTable(ProjectX.X1_TABLE_NAME);
	}

	@Override
	public void up() {
		createTable(table(ProjectX.X1_TABLE_NAME, column("id", Define.DataTypes.INTEGER, primarykey(), autoincrement()),
				column("x", Define.DataTypes.VARCHAR, length(123))));

	}

	private static Log log = Log.getLog(MySQLGenerator.class);

	public static void main(String[] args) {
		log.debug("fine");
		log.warn("warning");
		log.error("severe");
	}
}

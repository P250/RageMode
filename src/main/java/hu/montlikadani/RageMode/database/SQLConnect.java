package hu.montlikadani.ragemode.database;

import java.io.File;
import java.sql.SQLException;

public class SQLConnect extends Database {

	public SQLConnect(File file, String prefix) {
		super("jdbc:sqlite:" + file, null, null, prefix);

		createDefaultTable();
	}

	public void createDefaultTable() {
		try {
			if (getConnection() != null && getConnection().isConnected()) {
				String query = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "stats_players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255) , uuid VARCHAR(255) , kills INT(11) , axe_kills INT(11) , direct_arrow_kills INT(11) , explosion_kills INT(11) , knife_kills INT(11) , deaths INT(11) , axe_deaths INT(11) , direct_arrow_deaths INT(11) , explosion_deaths INT(11) , knife_deaths INT(11) , wins INT(11) , score INT(11) , games INT(11) , kd DOUBLE, UNIQUE(uuid));";
				getConnection().executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

package hu.montlikadani.ragemode.database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import hu.montlikadani.ragemode.Debug;

public class RMConnection {

	private Connection conn = null;

	public RMConnection(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() {
		return conn;
	}

	public boolean isConnected() {
		try {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			Debug.logConsole(e.getMessage());
		}

		return false;
	}

	public boolean isValid() {
		return isValid(2);
	}

	public boolean isValid(int timeout) {
		try {
			return conn != null && conn.isValid(timeout);
		} catch (SQLException e) {
			Debug.logConsole(e.getMessage());
		}

		return false;
	}

	public void close() throws SQLException {
		if (isConnected()) {
			conn.close();
		}
	}

	public synchronized void commit() throws SQLException {
		conn.commit();
	}

	public void executeUpdate(String query) throws SQLException {
		createStatement().executeUpdate(query);
	}

	public Statement createStatement() throws SQLException {
		return conn.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	public ResultSet executeQuery(Statement statement, String query) throws SQLException {
		return statement.executeQuery(query);
	}
}

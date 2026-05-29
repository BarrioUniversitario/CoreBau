package me.davidml16.baul.database;

import me.davidml16.baul.Main;
import me.davidml16.baul.database.types.DatabaseConnection;
import me.davidml16.baul.database.types.MySqlConnection;
import me.davidml16.baul.database.types.SQLiteConnection;
import me.davidml16.baul.objects.Cubelet;
import me.davidml16.baul.objects.Profile;
import me.davidml16.baul.objects.loothistory.LootHistory;
import me.davidml16.baul.objects.loothistory.RewardHistory;
import me.davidml16.baul.utils.ItemStack64;
import me.davidml16.baul.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseHandler {

	public interface Callback<T> {

		void done(T valor);

	}

	private DatabaseConnection databaseConnection;

	private Main main;

	public DatabaseHandler(Main main) {
		this.main = main;
		if(main.getConfig().getBoolean("MySQL.Enabled")) {
			databaseConnection = new MySqlConnection(main);
		} else {
			databaseConnection = new SQLiteConnection(main);
		}
	}

	public void openConnection() {
		Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format("  "));
		Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eLoading database:"));
		databaseConnection.open();
	}

	public void changeToSQLite() {
		databaseConnection = new SQLiteConnection(main);
		databaseConnection.open();
	}

	public DatabaseConnection getDatabaseConnection() { return databaseConnection; }

	public void executeQuery(String sql) {

		PreparedStatement statement = null;
		Connection connection = null;

		try {
			connection = databaseConnection.getConnection();
			statement = connection.prepareStatement(sql);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			databaseConnection.close(connection);
		}

	}

	public void executeQueryError(String sql) throws SQLException {

		PreparedStatement statement = null;
		Connection connection = null;

		try {
			connection = databaseConnection.getConnection();
			statement = connection.prepareStatement(sql);
			statement.execute();
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			databaseConnection.close(connection);
		}

	}

	public void loadTables() {

		executeQuery("CREATE TABLE IF NOT EXISTS ac_cubelets (`UUID` varchar(40) NOT NULL, `cubeletUUID` varchar(40) NOT NULL, `type` VARCHAR(255) NOT NULL, `received` bigint NOT NULL DEFAULT 0, `expire` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`UUID`, `cubeletUUID`));");

		executeQuery("CREATE TABLE IF NOT EXISTS ac_players (`UUID` varchar(40) NOT NULL, `NAME` varchar(40), `LOOT_POINTS` integer(25), `ORDER_BY` varchar(10), `ANIMATION` varchar(25), `COSMETICS_VISIBLE` integer DEFAULT 1, PRIMARY KEY (`UUID`));");

		// Backfill COSMETICS_VISIBLE column on existing installs. Both branches
		// fail-silently if the column already exists.
		try { executeQueryError("ALTER TABLE ac_players ADD COLUMN COSMETICS_VISIBLE integer DEFAULT 1"); } catch (SQLException ignored) {}

		try {
			executeQueryError("CREATE TABLE IF NOT EXISTS ac_loothistory (`ID` INTEGER PRIMARY KEY AUTO_INCREMENT, `UUID` varchar(40) NOT NULL, `cubeletName` varchar(255) NOT NULL, `rewardID` varchar(50) NOT NULL, `rewardName` varchar(255) NOT NULL, `rewardIcon` LONGTEXT NOT NULL, `received` bigint NOT NULL DEFAULT 0);");
		} catch (SQLException e) {
			try {
				executeQueryError("CREATE TABLE IF NOT EXISTS ac_loothistory (`ID` INTEGER PRIMARY KEY AUTOINCREMENT, `UUID` varchar(40) NOT NULL, `cubeletName` varchar(255) NOT NULL, `rewardID` varchar(50) NOT NULL, `rewardName` varchar(255) NOT NULL, `rewardIcon` LONGTEXT NOT NULL, `received` bigint NOT NULL DEFAULT 0);");
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}

		try {
			executeQueryError("ALTER TABLE ac_cubelets MODIFY type VARCHAR(255)");
		} catch (SQLException e) {}

		try {
			executeQueryError("ALTER TABLE ac_loothistory MODIFY rewardName VARCHAR(255)");
		} catch (SQLException e) {}

		try {
			executeQueryError("ALTER TABLE ac_loothistory MODIFY cubeletName VARCHAR(255)");
		} catch (SQLException e) {}

		executeQuery("CREATE TABLE IF NOT EXISTS ac_cosmetics_owned (`UUID` varchar(40) NOT NULL, `cosmeticId` varchar(64) NOT NULL, `unlockedAt` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`UUID`, `cosmeticId`));");

		executeQuery("CREATE TABLE IF NOT EXISTS ac_cosmetics_equipped (`UUID` varchar(40) NOT NULL, `category` varchar(32) NOT NULL, `cosmeticId` varchar(64) NOT NULL, PRIMARY KEY (`UUID`, `category`));");

		executeQuery("CREATE TABLE IF NOT EXISTS ac_emote_cooldowns (`UUID` varchar(40) NOT NULL, `emoteId` varchar(64) NOT NULL, `lastUsed` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`UUID`, `emoteId`));");

	}

	public CompletableFuture<Map<String, Long>> getEmoteCooldowns(UUID uuid) {
		CompletableFuture<Map<String, Long>> result = new CompletableFuture<>();
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			Map<String, Long> map = new HashMap<>();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT emoteId, lastUsed FROM ac_emote_cooldowns WHERE UUID = ?;");
				ps.setString(1, uuid.toString());
				rs = ps.executeQuery();
				while (rs.next()) map.put(rs.getString("emoteId"), rs.getLong("lastUsed"));
				result.complete(map);
			} catch (SQLException e) {
				e.printStackTrace();
				result.complete(map);
			} finally {
				if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
		return result;
	}

	public void setEmoteCooldown(UUID uuid, String emoteId, long timestamp) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("REPLACE INTO ac_emote_cooldowns (UUID, emoteId, lastUsed) VALUES (?, ?, ?);");
				ps.setString(1, uuid.toString());
				ps.setString(2, emoteId);
				ps.setLong(3, timestamp);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
	}

	public CompletableFuture<Set<String>> getOwnedCosmetics(UUID uuid) {
		CompletableFuture<Set<String>> result = new CompletableFuture<>();
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			Set<String> owned = new HashSet<>();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT cosmeticId FROM ac_cosmetics_owned WHERE UUID = ?;");
				ps.setString(1, uuid.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					owned.add(rs.getString("cosmeticId"));
				}
				result.complete(owned);
			} catch (SQLException e) {
				e.printStackTrace();
				result.complete(owned);
			} finally {
				if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
		return result;
	}

	public CompletableFuture<Map<String, String>> getEquippedCosmetics(UUID uuid) {
		CompletableFuture<Map<String, String>> result = new CompletableFuture<>();
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			Map<String, String> equipped = new HashMap<>();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT category, cosmeticId FROM ac_cosmetics_equipped WHERE UUID = ?;");
				ps.setString(1, uuid.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					equipped.put(rs.getString("category"), rs.getString("cosmeticId"));
				}
				result.complete(equipped);
			} catch (SQLException e) {
				e.printStackTrace();
				result.complete(equipped);
			} finally {
				if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
		return result;
	}

	public void addOwnedCosmetic(UUID uuid, String cosmeticId, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("INSERT INTO ac_cosmetics_owned (UUID, cosmeticId, unlockedAt) VALUES (?, ?, ?);");
				ps.setString(1, uuid.toString());
				ps.setString(2, cosmeticId);
				ps.setLong(3, System.currentTimeMillis());
				ps.executeUpdate();
			} catch (SQLException e) {
				// Duplicate key is expected when re-granting; quietly tolerate.
				if (!isDuplicateKey(e)) e.printStackTrace();
			} finally {
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
				if (callback != null) Bukkit.getScheduler().runTask(main, callback);
			}
		});
	}

	public void removeOwnedCosmetic(UUID uuid, String cosmeticId) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cosmetics_owned WHERE UUID = ? AND cosmeticId = ?;");
				ps.setString(1, uuid.toString());
				ps.setString(2, cosmeticId);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
	}

	public void setEquippedCosmetic(UUID uuid, String category, String cosmeticId, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				// REPLACE INTO is supported by both MySQL and SQLite for primary-key conflicts.
				ps = connection.prepareStatement("REPLACE INTO ac_cosmetics_equipped (UUID, category, cosmeticId) VALUES (?, ?, ?);");
				ps.setString(1, uuid.toString());
				ps.setString(2, category);
				ps.setString(3, cosmeticId);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
				if (callback != null) Bukkit.getScheduler().runTask(main, callback);
			}
		});
	}

	public void unequipCosmetic(UUID uuid, String category, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cosmetics_equipped WHERE UUID = ? AND category = ?;");
				ps.setString(1, uuid.toString());
				ps.setString(2, category);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
				if (callback != null) Bukkit.getScheduler().runTask(main, callback);
			}
		});
	}

	private boolean isDuplicateKey(SQLException e) {
		// MySQL 1062, SQLite 19 (CONSTRAINT_PRIMARYKEY = 1555).
		int code = e.getErrorCode();
		String state = e.getSQLState();
		return code == 1062 || code == 19 || code == 1555 || (state != null && state.startsWith("23"));
	}

	public void hasName(String name, Callback<String> callback) throws SQLException {

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;

			try {

				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_players WHERE LOWER(NAME) = '" + name.toLowerCase() + "';");
				rs = ps.executeQuery();

				if (rs.next()) {
					callback.done(rs.getString("NAME"));
				} else {
					callback.done(null);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}

		});

	}

	public void createPlayerData(Player p) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("INSERT INTO ac_players (UUID,NAME,LOOT_POINTS,ORDER_BY,ANIMATION,COSMETICS_VISIBLE) VALUES(?,?,?,?,?,?)");
				ps.setString(1, p.getUniqueId().toString());
				ps.setString(2, p.getName());
				ps.setLong(3, 0);
				ps.setString(4, "date");
				ps.setString(5, "animation2");
				ps.setInt(6, 1);
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void getPlayerAnimation(UUID uuid, Callback<String> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_players WHERE UUID = '" + uuid + "';");
				rs = ps.executeQuery();

				String animation = "animation2";

				if (rs.next()) {
					animation = rs.getString("ANIMATION");
				}

				String finalAnimation = animation;
				Bukkit.getScheduler().runTask(main, () -> callback.done(finalAnimation));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void setPlayerAnimation(UUID uuid, String animation) {

		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("UPDATE ac_players SET `ANIMATION` = ? WHERE `UUID` = ?");
				ps.setString(1, animation);
				ps.setString(2, uuid.toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}

		});

	}

	public void getPlayerOrderSetting(UUID uuid, Callback<String> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_players WHERE UUID = '" + uuid + "';");
				rs = ps.executeQuery();

				String order = "date";

				if (rs.next()) {
					order =  rs.getString("ORDER_BY");
				}

				String finalOrder = order;
				Bukkit.getScheduler().runTask(main, () -> callback.done(finalOrder));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void getPlayerCosmeticsVisible(UUID uuid, Callback<Boolean> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT COSMETICS_VISIBLE FROM ac_players WHERE UUID = ?;");
				ps.setString(1, uuid.toString());
				rs = ps.executeQuery();
				boolean visible = true;
				if (rs.next()) visible = rs.getInt("COSMETICS_VISIBLE") != 0;
				final boolean fv = visible;
				Bukkit.getScheduler().runTask(main, () -> callback.done(fv));
			} catch (SQLException e) {
				e.printStackTrace();
				Bukkit.getScheduler().runTask(main, () -> callback.done(true));
			} finally {
				if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
				if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
				databaseConnection.close(connection);
			}
		});
	}

	public void getPlayerLootPoints(UUID uuid, Callback<Long> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_players WHERE UUID = '" + uuid + "';");
				rs = ps.executeQuery();

				long amount = 0;

				if (rs.next()) {
					amount = rs.getLong("LOOT_POINTS");
				}

				long finalAmount = amount;
				Bukkit.getScheduler().runTask(main, () -> callback.done(finalAmount));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public long getSyncLootPoints(UUID uuid) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = databaseConnection.getConnection();
			ps = connection.prepareStatement("SELECT LOOT_POINTS FROM ac_players WHERE UUID = ?");
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			if (rs.next()) return rs.getLong("LOOT_POINTS");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
			if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
			databaseConnection.close(connection);
		}
		return 0;
	}

	public void setPlayerLootPoints(UUID uuid, long amount) {
		setPlayerLootPoints(uuid, amount, null);
	}

	public void setPlayerLootPoints(UUID uuid, long amount, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("UPDATE ac_players SET `LOOT_POINTS` = ? WHERE `UUID` = ?");
				ps.setLong(1, amount);
				ps.setString(2, uuid.toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
				if (callback != null) {
					Bukkit.getScheduler().runTask(main, callback);
				}
			}
		});
	}

	public void saveProfileAsync(final Profile profile) {
		saveProfileAsync(profile, null);
	}

	public void saveProfileAsync(final Profile profile, final String playerName) {

		if(profile == null) return;

		String name = playerName;
		if (name == null) {
			Player player = Bukkit.getPlayer(profile.getUuid());
			if (player != null) name = player.getName();
		}

		final String finalName = name;

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("UPDATE ac_players SET `NAME` = ?, `LOOT_POINTS` = ?, `ORDER_BY` = ?, `ANIMATION` = ?, `COSMETICS_VISIBLE` = ? WHERE `UUID` = ?");
				ps.setString(1, finalName != null ? finalName : "unknown");
				ps.setLong(2, profile.getLootPoints());
				ps.setString(3, profile.getOrderBy());
				ps.setString(4, profile.getAnimation());
				ps.setInt(5, profile.isCosmeticsVisible() ? 1 : 0);
				ps.setString(6, profile.getUuid().toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void saveProfileSync(final Profile profile) {

		if(profile == null) return;

		String name = Bukkit.getPlayer(profile.getUuid()).getName();

		PreparedStatement ps = null;
		Connection connection = null;
		try {
			connection = databaseConnection.getConnection();
			ps = connection.prepareStatement("UPDATE ac_players SET `NAME` = ?, `LOOT_POINTS` = ?, `ORDER_BY` = ?, `ANIMATION` = ?, `COSMETICS_VISIBLE` = ? WHERE `UUID` = ?");
			ps.setString(1, name);
			ps.setLong(2, profile.getLootPoints());
			ps.setString(3, profile.getOrderBy());
			ps.setString(4, profile.getAnimation());
			ps.setInt(5, profile.isCosmeticsVisible() ? 1 : 0);
			ps.setString(6, profile.getUuid().toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
			databaseConnection.close(connection);
		}
	}

	public void updatePlayerName(Player p) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("UPDATE ac_players SET `NAME` = ? WHERE `UUID` = ?");
				ps.setString(1, p.getName());
				ps.setString(2, p.getUniqueId().toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void getPlayerUUID(String name, Callback<String> callback) throws SQLException {
		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_players WHERE LOWER(NAME) = '" + name.toLowerCase() + "';");
				rs = ps.executeQuery();

				if (rs.next()) {
					final String uuid = rs.getString("UUID");
					Bukkit.getScheduler().runTask(main, () -> callback.done(uuid));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void addCubelets(UUID uuid, Collection<Cubelet> cubelets) {
		addCubelets(uuid, cubelets, null);
	}

	public void addCubelets(UUID uuid, Collection<Cubelet> cubelets, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			StringBuilder insertString = new StringBuilder();
			for(Cubelet cubelet : cubelets) {
				if(insertString.toString().equalsIgnoreCase("")) insertString.append("('").append(uuid.toString()).append("','").append(cubelet.getUuid()).append("','").append(cubelet.getType()).append("',").append(cubelet.getReceived()).append(",").append(cubelet.getExpire()).append(")");
				else insertString.append(", ('").append(uuid.toString()).append("','").append(cubelet.getUuid()).append("','").append(cubelet.getType()).append("',").append(cubelet.getReceived()).append(",").append(cubelet.getExpire()).append(")");
			}

			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("INSERT INTO ac_cubelets (UUID,cubeletUUID,type,received,expire) VALUES " + insertString.toString());
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
				if (callback != null) {
					Bukkit.getScheduler().runTask(main, callback);
				}
			}
		});
	}

	public void removeCubelet(UUID uuid, UUID cubeletUUID) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE UUID = '" + uuid + "' AND cubeletUUID = '" + cubeletUUID + "';");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void removeCubelet(UUID uuid, String type, int amount) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE rowid IN (SELECT rowid FROM ac_cubelets WHERE UUID = '" + uuid + "' AND type = '" + type + "' LIMIT " + amount + ");");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void removeCubelets(UUID uuid, Collection<Cubelet> cubelets) {
		removeCubelets(uuid, cubelets, null);
	}

	public void removeCubelets(UUID uuid, Collection<Cubelet> cubelets, Runnable callback) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			StringBuilder deleteString = new StringBuilder();
			for(Cubelet cubelet : cubelets) {
				if(deleteString.toString().equalsIgnoreCase("")) deleteString.append("'").append(cubelet.getUuid().toString()).append("'");
				else deleteString.append(",'").append(cubelet.getUuid().toString()).append("'");
			}

			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE UUID = '" + uuid + "' AND cubeletUUID IN (" + deleteString + ");");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
				if (callback != null) {
					Bukkit.getScheduler().runTask(main, callback);
				}
			}
		});
	}

	public void removeCubelets(UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE UUID = '" + uuid + "';");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void removeCubelet(String type) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE type = '" + type + "';");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void removeExpiredCubelets(UUID uuid) {
		long actualTime = System.currentTimeMillis();
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			PreparedStatement ps = null;
			Connection connection = null;
			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_cubelets WHERE UUID = '" + uuid + "' AND expire != -1 AND expire < '" + actualTime + "';");
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}
		});
	}

	public void getCubeletBalance(UUID uuid, String cubeletType, Callback<Long> callback) {

		Bukkit.getScheduler().runTaskAsynchronously(main, (Runnable) () -> {

			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;

			try {
				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT COUNT(*) AS amount FROM ac_cubelets WHERE UUID = '" + uuid.toString() + "' AND type = '" + cubeletType + "';");
				rs = ps.executeQuery();

				if (rs.next()) {
					final Long ammount = Long.valueOf(rs.getInt("amount"));
					Bukkit.getScheduler().runTask(main, () -> callback.done(ammount));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				databaseConnection.close(connection);
			}

		});

	}

	public CompletableFuture<List<Cubelet>> getCubelets(UUID uuid) {

		CompletableFuture<List<Cubelet>> result = new CompletableFuture<>();

		long actualTime = System.currentTimeMillis();

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			List<Cubelet> cubelets = new ArrayList<>();

			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;

			try {

				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_cubelets WHERE UUID = '" + uuid.toString() + "' AND (expire = -1 OR expire > '" + actualTime + "');");

				rs = ps.executeQuery();
				while (rs.next()) {
					if(main.getCubeletTypesHandler().getTypes().containsKey(rs.getString("type")))
						cubelets.add(new Cubelet(UUID.fromString(rs.getString("cubeletUUID")), rs.getString("type"), rs.getLong("received"), rs.getLong("expire")));
				}

				result.complete(cubelets);

			} catch (SQLException e) {

				e.printStackTrace();

			} finally {

				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				databaseConnection.close(connection);

			}
		});

		return result;

	}

	public CompletableFuture<List<LootHistory>> getLootHistory(UUID uuid) {

		CompletableFuture<List<LootHistory>> result = new CompletableFuture<>();

		long actualTime = System.currentTimeMillis();

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			List<LootHistory> lootHistory = new ArrayList<>();

			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection connection = null;

			try {

				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("SELECT * FROM ac_loothistory WHERE UUID = '" + uuid.toString() + "';");

				rs = ps.executeQuery();

				while (rs.next()) {

					int id = rs.getInt("ID");
					String playerUUID = rs.getString("UUID");
					String cubeletName = rs.getString("cubeletName");

					long received = rs.getLong("received");

					String rewardID = rs.getString("rewardID");
					String rewardName = rs.getString("rewardName");
					String rewardIcon = rs.getString("rewardIcon");

					RewardHistory rewardHistory = new RewardHistory(UUID.fromString(rewardID), rewardName, rewardIcon);

					lootHistory.add(new LootHistory(id, UUID.fromString(playerUUID), cubeletName, received, rewardHistory));

				}

				result.complete(lootHistory);

			} catch (SQLException e) {

				e.printStackTrace();

			} finally {

				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				if(rs != null) {
					try {
						rs.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				databaseConnection.close(connection);

			}

		});

		return result;

	}

	public void addLootHistory(UUID uuid, LootHistory lootHistory) {

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			PreparedStatement ps = null;
			Connection connection = null;

			try {

				String iconBase64 = ItemStack64.itemStackToBase64(lootHistory.getRewardHistory().getItemStack());

				String value =
						"('"
						+ uuid.toString()
						+ "','"
						+ lootHistory.getCubeletName()
						+ "','"
						+ lootHistory.getRewardHistory().getUUID()
						+ "','"
						+ lootHistory.getRewardHistory().getName()
						+ "','"
						+ iconBase64
						+ "',"
						+ lootHistory.getReceived()
						+ ")";

				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("INSERT INTO ac_loothistory (UUID,cubeletName,rewardID,rewardName,rewardIcon,received) VALUES " + value);
				ps.executeUpdate();

			} catch (SQLException e) {

				e.printStackTrace();

			} finally {

				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				databaseConnection.close(connection);

			}

		});

	}

	public void removeLootHistory(UUID uuid) {

		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

			PreparedStatement ps = null;
			Connection connection = null;

			try {

				connection = databaseConnection.getConnection();
				ps = connection.prepareStatement("DELETE FROM ac_loothistory WHERE UUID = '" + uuid.toString() + "';");
				ps.execute();

			} catch (SQLException e) {

				e.printStackTrace();

			} finally {

				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException throwables) {
						throwables.printStackTrace();
					}
				}

				databaseConnection.close(connection);

			}

		});

	}

}

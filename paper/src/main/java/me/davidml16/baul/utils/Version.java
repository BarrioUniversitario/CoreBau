package me.davidml16.baul.utils;


import lombok.Getter;
import org.bukkit.Bukkit;


/**
 * Enum of supported NMS versions.
 */


@SuppressWarnings( "java:S115" ) // SonarLint: Enum values naming convention
public final class Version {

	@Getter
	private static V currentVersion;

	@Getter
	private static int subVersion;

	private static String serverVersion;

	// Static initializer
	static {
		initializeVersionInfo();
	}

	// Comparison methods

	public static boolean equals(V version) {
		return compare(version) == 0;
	}

	public static boolean olderThan(V version) {
		return compare(version) < 0;
	}

	public static boolean newerThan(V version) {
		return compare(version) > 0;
	}

	public static boolean atLeast(V version) {
		return !olderThan(version);
	}

	private static int compare(V version) {
		try {
			return getCurrentVersion().minor - version.minor;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static String getFullVersion() {
		return currentVersion + (subVersion > 0 ? "." + subVersion : "");
	}

	/**
	 * @deprecated Returns empty on Paper 1.20.5+, prefer {@link #getFullVersion()}
	 */
	@Deprecated
	public static String getServerVersion() {
		return "craftbukkit".equals(serverVersion) ? "" : serverVersion;
	}

	private static void initializeVersionInfo() {
		String pkgName = Bukkit.getServer() != null ? Bukkit.getServer().getClass().getPackage().getName() : "";

		String versionPart = pkgName.isEmpty() ? "" : pkgName.substring(pkgName.lastIndexOf('.') + 1);
		serverVersion = "craftbukkit".equals(versionPart) ? "" : versionPart;

		String[] versionTokens = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
		if (versionTokens.length < 2 || versionTokens.length > 3) {
			throw new RuntimeException("Unrecognized Bukkit version format: " + String.join(".", versionTokens));
		}

		int minor = Integer.parseInt(versionTokens[1]);
		currentVersion = minor < 3 ? V.v1_3_AND_BELOW : V.fromMinor(minor);
		subVersion = versionTokens.length == 3 ? Integer.parseInt(versionTokens[2]) : 0;
	}

	public enum V {
		v1_22(22),
		v1_21(21),
		v1_20(20),
		v1_19(19),
		v1_18(18),
		v1_17(17),
		v1_16(16),
		v1_15(15),
		v1_14(14),
		v1_13(13),
		v1_12(12),
		v1_11(11),
		v1_10(10),
		v1_9(9),
		v1_8(8),
		v1_7(7),
		v1_6(6),
		v1_5(5),
		v1_4(4),
		v1_3_AND_BELOW(3);

		@Getter
		private final int minor;

		V(int minor) {
			this.minor = minor;
		}

		public static V fromMinor(int minor) {
			for (V version : values()) {
				if (version.minor == minor) return version;
			}
			throw new RuntimeException("Unsupported Minecraft version: 1." + minor);
		}

		@Override
		public String toString() {
			return "1." + minor;
		}
	}
}
package dev.blancocl.skin;

import dev.blancocl.api.skin.Skin;
import dev.blancocl.api.skin.SkinSource;

/**
 * Hard-coded Steve fallback so the plugin can render NPCs even with no
 * network access or while skins are still loading.
 */
public final class FallbackSkins {

    private FallbackSkins() {}

    public static final Skin STEVE = new Skin(
            "MHF_Steve",
            "ewogICJ0aW1lc3RhbXAiIDogMTcwMDAwMDAwMDAwMCwKICAicHJvZmlsZUlkIiA6ICJjMDZmODkwNjRjOGE0OWZkOGFkNGVjY2VkMDBmOWRlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3RldmUiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEzNTZjOWZkMTEwYWQ0NjFkOTRiYjVlMTcyNDRkN2I5NjI0MzNkNzM2OWQ4ZTBhZmNlYWUxMGJjN2RkNDA1YyIKICAgIH0KICB9Cn0=",
            "QnogV2hpdGVzcGFjZSBzaWduYXR1cmUgcGxhY2Vob2xkZXIgLSByZXBsYWNlIHdpdGggcmVhbCBNb2phbmcgc2lnbmF0dXJlIGF0IGZpcnN0IGZldGNoLg==",
            SkinSource.FALLBACK, 0L);
}

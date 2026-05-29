-- Esquema MySQL para el backend "mysql" del plugin Npc.
-- Una tabla, un blob JSON por NPC. Hikari ejecuta este script al arrancar.
CREATE TABLE IF NOT EXISTS npc (
  id          VARCHAR(64)  NOT NULL PRIMARY KEY,
  data        JSON         NOT NULL,
  updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

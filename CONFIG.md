# CONFIG.md - Apotheosis Self Affix 設定説明書 / Configuration Guide

## 日本語 (Japanese)

本MODの設定は `config/apotheosis_self_affix-common.toml` に保存されます。

### 設定項目一覧

| 設定キー | データ型 | デフォルト値 | 説明 |
|---|---|---|---|
| `autoEquipOnJoin` | Boolean | `true` | 初回ログイン時、魂スロットに魂が装備されていない場合に自動で魂アイテムを付与・装備するかどうか。 |
| `keepOnDeath` | Boolean | `true` | プレイヤー死亡時に、魂アイテム（NBTやアフィックスを含む）を消滅・ドロップさせずにリスポーン先へ引き継ぐかどうか。 |
| `durableXpMultiplier` | Double (0.0 ~ 100.0) | `1.0` | 耐久力を持たない魂アイテムにおいて、Durable（耐久免除）アフィックスを経験値ボーナスへ変換する倍率。`1.0` で100%変換（免除率25%なら経験値+25%）。 |
| `debugLog` | Boolean | `false` | アフィックス発動や引き継ぎ処理の詳細なログをコンソールに出力するかどうか。 |

---

## English

Configuration for this mod is located in `config/apotheosis_self_affix-common.toml`.

### Configuration Options

| Key | Data Type | Default | Description |
|---|---|---|---|
| `autoEquipOnJoin` | Boolean | `true` | Whether to automatically create and equip a Soul in the Soul slot when a player joins and has no Soul equipped. |
| `keepOnDeath` | Boolean | `true` | Whether the Soul item (including all NBT and affixes) is preserved across player death without dropping or being lost. |
| `durableXpMultiplier` | Double (0.0 ~ 100.0) | `1.0` | Multiplier used when substituting the Durable affix (durability loss reduction) into bonus experience gain. `1.0` equals 100% conversion (e.g. 25% durability bonus gives +25% bonus XP). |
| `debugLog` | Boolean | `false` | Whether to output detailed affix trigger and transfer events into the game log. |

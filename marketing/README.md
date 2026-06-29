# Marketing assets

Generated procedurally (same duck as the plugin) — no external art. Regenerate with:

```bash
java -Djava.awt.headless=true marketing/gen/DuckImageGen.java marketing
```

| File | Size | Use on Marketplace |
|---|---|---|
| `pluginIcon-512.png` | 512×512 | Avatar / social preview. (The in-IDE logo is `src/main/resources/META-INF/pluginIcon.svg`.) |
| `preview-moods.png` | 1400×460 | Screenshot #1 — the four moods at a glance. |
| `preview-editor.png` | 1280×800 | Screenshot #2 — duck in the editor corner reacting to a broken build. |
| `preview-tell-the-duck.png` | 900×560 | Screenshot #3 — the "Tell the Duck" ritual popup. |

Tip: lead with `preview-editor.png` or `preview-moods.png` — they communicate the concept fastest.
The real in-IDE screenshots (once you run it live) will look even better; swap them in when handy.

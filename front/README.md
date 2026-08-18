# spalod

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

## Environment Variables

Environment variables are read by Vite from `front/.env` (development) and
`front/.env_prod` (production). Values that must not be committed belong in
`front/.env.local`, which is excluded from version control.

| Variable | Purpose |
|---|---|
| `VITE_APP_API_BASE_URL` | Base URL of the SpaLod backend API. |
| `VITE_APP_GRAPH_DB` | Base URL of the GraphDB instance. |
| `VITE_APP_FLYVAST_POINTCLOUD_VIEWER_BASE_URL` | Base URL of the external point cloud viewer. |
| `VITE_MAPBOX_TOKEN` | Mapbox access token used by the map view basemap. |

`VITE_MAPBOX_TOKEN` has no default value. Without it the Mapbox basemap layers
stay empty; the OpenTopoMap and OpenStreetMap layers keep working. Create your
own token at https://account.mapbox.com and store it in `front/.env.local`:

```sh
VITE_MAPBOX_TOKEN=your_token_here
```

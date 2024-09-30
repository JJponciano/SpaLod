<template>
  <div class="features">
    <div class="navigation">
      <template v-for="(nav, index) in navigation" :key="nav.name">
        <a :href="nav.url">{{ nav.name }}</a>
        <span v-if="navigation.length > 1 && index != navigation.length - 1"> > </span>
      </template>
    </div>
    <h1>{{ getTitle() }}</h1>
    <table>
      <thead>
        <tr>
          <th v-for="key in header" class="head">{{ key.charAt(0).toUpperCase() + key.slice(1) }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(feature, index) in features" :key="index">
          <td v-for="key in header" @click="urlClick(feature[key], key)"
            :class="{ clickable: key === 'collections' || key === 'dataset' || key === 'conformance' || key === 'URL' }"
            class="row">
            <template v-if="key === 'JSON'">
              <button @click="downloadJson(feature.JSON, feature.Feature)">DOWNLOAD JSON</button>
            </template>
            <template v-else-if="key === 'HTML'">
              <button
                @click="feature[key] ? urlClick(feature[key], key) : urlClick(feature['collections'], 'collections')"
                class="view-html-button">VIEW HTML</button>
            </template>
            <template v-else>
              {{ feature[key] }}
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { $ajax } from '../services/api';

export default {
  data() {
    return {
      header: [],
      features: [],
      navigation: [],
    }
  },
  mounted() {
    const url = new URL(window.location.href);
    $ajax({
      headers: {
        'Content-Type': 'application/json'
      },
      url: import.meta.env.VITE_APP_API_BASE_URL + '/api' + url.pathname + '/',
      type: 'GET',
      dataType: 'json',
      success: this.handleResponse,
      error: (error) => {
        console.log(error);
      }
    });
    this.getNavigation();
  },
  methods: {
    getTitle() {
      const url = new URL(window.location.href);
      const path = url.pathname.split('/');
      return path[path.length - 1] === 'spalodWFS' ? 'Landing Page of SpaLod WFS Service' : path[path.length - 1].charAt(0).toLocaleUpperCase() + path[path.length - 1].slice(1);
    },
    getNavigation() {
      const url = new URL(window.location.href);
      const path = url.pathname.split('/');
      var localUrl = '';
      path.forEach((element, index) => {
        if (index > 0) {
          localUrl += '/' + element;
          this.navigation.push({
            name: element.charAt(0).toLocaleUpperCase() + element.slice(1),
            url: localUrl,
          });
        }
      });
      console.log(this.navigation);
    },
    handleResponse(response) {
      console.log(JSON.stringify(response));
      this.header = response['head']['vars'];
      response.results.bindings.forEach(feature => {
        const featureObject = {};
        this.header.forEach((key) => {
          featureObject[key] = feature[key].value;
        });
        this.features.push(featureObject);
      });
      if (this.getTitle() === 'Collections') {
        this.header.push('HTML');
        console.log(this.header);
      }
    },
    urlClick(url, head) {
      if (url.startsWith('https://') || url.startsWith('http://')) {
        if (this.getTitle() === 'Conformance') {
          window.open(url, '_blank').focus();
        } else if (head === 'collections') {
          window.location.href = '/spalodWFS/collections/' + url.split('#')[1];
        } else if (head === 'dataset') {
          var collectionId = window.location.href;
          if (collectionId.includes('/collections/')) {
            collectionId = collectionId.split('/collections/')[1];
            if (collectionId.includes('/items')) {
              collectionId = collectionId.split('/items')[0];
            }
          } else {
            collectionId = 'undefined';
          }
          window.location.href = '/collections/' + collectionId + '/items/' + url.split('#')[1];
        } else {
          window.location.href = url;
        }
      }
    },
    downloadJson(url, feature) {
      console.log(url);
      $ajax({
        headers: {
          'Content-Type': 'application/json'
        },
        url: url,
        type: 'POST',
        dataType: 'json',
        success: (response) => {
          const json = JSON.stringify(response);
          const url = window.URL.createObjectURL(new Blob([json]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', feature + ".json");
          document.body.appendChild(link);
          link.click();
        },
        error: (error) => {
          console.log(error);
        }
      });
    },
  }
}
</script>

<style>
.features {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100vw;
  height: fit-content;
  padding: 20px;
}

h1 {
  text-align: center;
  font-weight: bold;
  font-size: 2rem;
  margin-bottom: 20px;
}

.table,
.head,
.row {
  border: none;
}

.mainOGC {
  top: 100px;
}

.navigation {
  margin: 100px 0;
  font-size: 20px;
}

.navigation a {
  color: #0baaa7;
}
</style>
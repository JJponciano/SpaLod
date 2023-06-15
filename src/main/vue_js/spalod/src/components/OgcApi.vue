<template>
<div class="features">
    <h1>{{ getTitle() }}</h1>
    <table>
          <thead>
            <tr>
              <th v-for="key in header" class="head">{{ key.charAt(0).toUpperCase() + key.slice(1) }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(feature, index) in features" :key="index">
              <td v-for="key in header" @click="urlClick(feature[key], key)" :class="{ clickable: key === 'collections' || key === 'dataset' || key === 'conformance' || key === 'URL' }" class="row">
                  <template v-if="key === 'JSON'">
                    <button @click="downloadJson(feature.JSON, feature.Feature)">DOWNLOAD JSON</button>
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
import $ from "jquery";

export default {
    data() {
        return {
            header: [],
            features: [],
        }
    },
    mounted() {
        const url = new URL(window.location.href);
        $.ajax({
            headers: {
                'Content-Type': 'application/json'
            },
            url: 'https://localhost:8081/api' + url.pathname + '/',
            type: 'POST',
            dataType: 'json',
            success: this.handleResponse,
            error: (error) => {
                console.log(error);
            }
        });
    },
    methods: {
        getTitle() {
            const url = new URL(window.location.href);
            const path = url.pathname.split('/');
            return path[path.length - 1] === 'spalodWFS' ? 'Landing Page of SpaLod WFS Service' : path[path.length - 1].charAt(0).toLocaleUpperCase() + path[path.length - 1].slice(1);
        },
        handleResponse(response) {
            console.log(JSON.stringify(response));
            this.header = response['head']['vars'];
            response.results.bindings.forEach(feature => {
                const featureObject = {};
                this.header.forEach((key) => {
                    console.log(feature[key])
                    featureObject[key] = feature[key].value;
                });
                this.features.push(featureObject);
            });
            console.log(this.header);
        },
        urlClick(url, head) {
            if(url.startsWith('https://') || url.startsWith('http://')) {
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
                    window.location.href = '/spalodWFS/collections/' + collectionId + '/items/' + url.split('#')[1];
                } else if (head === 'URL') {
                    window.location.href = url.includes('/collections') ? '/spalodWFS/collections' : '/spalodWFS/conformance';
                }
            }
        },
        downloadJson(url, feature) {
            console.log(url);
            $.ajax({
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
    height: 100vh;
    padding: 20px;
}

h1 {
    text-align: center;
    font-weight: bold;
    font-size: 2rem;
    margin-bottom: 20px;
}

.table, .head, .row {
    border: none;
}
</style>
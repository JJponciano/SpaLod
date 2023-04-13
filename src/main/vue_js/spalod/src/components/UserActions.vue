<template>
    <div class="user-actions" :class="{dark: isDarkMode}">
        <div class="search-bar">
            <input type="text" v-model="searchText" placeholder="Search..." />
            <button class="search-button" @click="search">Search</button>
        </div>
        <button @click="filterData">Filter</button>
        <button @click="addData">Add Data</button>
        <button @click="confirmRequest">Confirm Request</button>
    </div>
</template>

<script>
export default {
    data() {
        return {
            searchText: '',
            isDarkMode: false,
        };
    },
    mounted() {
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
    },
    methods: {
        search() {
            // TODO: Implement search
        },
        filterData() {
            // TODO: Implement filter
        },
        addData() {
            // TODO: Implement add data
        },
        confirmRequest() {
            // TODO: Implement confirm request
        },
    },
};
</script>

<style scoped>
.user-actions {
    padding: 20px;
    border-radius: 5px;
    flex-direction: column;
    display: flex;
    align-items: start;
}

.user-actions.dark {
    background-color: #1A202C;
    color: #fff;
}

.user-actions.light {
    background-color: #fff;
    color: #1A202C;
}

.search-bar {
    display: flex;
    margin-bottom: 20px;
}

input[type="text"] {
    flex: 1;
    padding: 10px;
    border: none;
    border-radius: 5px 0 0 5px;
    background-color: #E5E7EB;
    color: #1F2937;
}

.search-button {
    padding: 10px 20px;
    border-radius: 0 5px 5px 0;
    border: none;
    background-color: #4A5568;
    color: #fff;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
}

button {
    padding: 10px 20px;
    border-radius: 5px;
    border: none;
    background-color: transparent;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
}

button:hover {
    background-color: #4A5568;
    color: #fff;
}
</style>
<template>
    <div class="popup-overlay" v-if="isPopupVisible" @click="closePopup"></div>
    <div v-bind:class="{ 'popup':true, 'visible': isPopupVisible}" :class="{dark:isDarkMode}">
        <div class="Title">
            <p>Add a new Catalog</p>
        </div>
        <div class="Name">
            <p>Name</p>
            <textarea v-model="inputName" :placeholder="placeholders" spellcheck="false"></textarea>
        </div>
        <div class="Description">
            <p>Description</p>
            <textarea v-model="inputDesc" :placeholder="placeholdersDesc" spellcheck="false"></textarea>
        </div>
        <div class="buttons">
            <button @click="closePopup">Close</button>
            <button class="confirm" @click="addCatalog">Add</button>
        </div>
    </div>
</template>

<script>
export default{
    props:{
        popupC: Boolean,
    },
    watch:{
        popupC(newValue){
            if(newValue){
                this.showPopup();
                this.inputName = '';
                this.inputDesc = '';
            }
        },
    },
    data(){
        return{
            isDarkMode: false,
            isPopupVisible: false,
            placeholders: "Name",
            placeholdersDesc: "Description",
            inputName: '',
            inputDesc: '',
        };
    },
    mounted(){
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
    },
    methods:{
        detectDarkMode(){
            this.isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
        },
        showPopup(){
            this.isPopupVisible=true;
        },
        closePopup(){
            this.isPopupVisible=false;
            this.$emit('popupCBack')
        },
        addCatalog(){
            var data = {
                name: this.inputName.replace(/ /g, '_'),
                desc: this.inputDesc.replace(/ /g, '_'),
                id: this.uuidv4(),
                publisher: localStorage.getItem("uuid") || "",
            };
            this.$emit('Catalog-data', data);
            this.closePopup();
        },
        uuidv4() {
            return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );
        },
    },
}
</script>

<style scoped>
.popup-overlay{
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: transparent;
}
.popup{
    display: none;
    background-color: white;
    box-shadow: 0px 0px 15px rgba(0, 0, 0, 0.5);
    border-style:solid;
    border-radius: 8px;
    border-width: 1px;
    cursor: default;
    z-index: 900;
    padding: 10px;
}
.popup.dark{
    background-color: #1A202C;
    color: white;
}
.popup.visible{
    display: block;
    width: 100%;
}
.Title p{
    font-size: large;
    text-align: center;
}
.Name textarea{
    resize: none;
    width: 100%;
    height: 25px;
}
.Description textarea{
    resize: none;
    height: 11vh;
}
.buttons{
    text-align: center;
}
.buttons button{
    font-size: 15px;
    margin: 0 10px;
}
.confirm{
    background-color: #0baaa7;
}
.confirm:hover{
    background-color: #4A5568;
}
</style>
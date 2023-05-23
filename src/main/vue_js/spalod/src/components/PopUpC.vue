<template>
    <div class="popup-overlay" v-if="isPopupVisible" @click="closePopup"></div>
    <div v-bind:class="{ 'popup':true, 'visible': isPopupVisible}" :class="{dark:isDarkMode}">
        <div class="Title">
            <p>Add a new Catalog</p>
        </div>
        <div class="Name">
            <p>Title</p>
            <textarea v-model="inputName" :placeholder="placeholders" spellcheck="false"></textarea>
        </div>
        <div class="Description">
            <p>Description</p>
            <textarea v-model="inputDesc" :placeholder="placeholdersDesc" spellcheck="false"></textarea>
        </div>
        <div class="Dataset">
            <p>Dataset</p>
        </div>
    </div>
</template>

<script>
export default{
    props:{
        popupC: Boolean,
        name: String,
    },
    watch:{
        popupC(newValue){
            if(newValue){
                this.showPopup();
            }
        },
        name(newValue){
            if(newValue){
                this.inputName=newValue;
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
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        //ajouter le publisher automatiquement, c'est a dire le nom et l'id de la personne qui a créer le tacalog
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
}
.popup.visible{
    display: block;
}
</style>
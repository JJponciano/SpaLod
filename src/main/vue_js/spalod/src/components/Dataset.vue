<template>
  <div>
    <select v-model="selected">
      <option disabled value="">Please select one Dataset</option>
      <option v-for="item in data" :key="item.value" :value="item.value">
        {{ item.label }}
      </option>
    </select>
  </div>
</template>

<script>
import { ref, onMounted, watch } from "vue";
import axios from "axios";

export default {
  name: "ComboBox",
  setup(_, { emit }) {
    const data = ref([]);
    const selected = ref(null);

    onMounted(async () => {
      $.ajax({
        url:
          import.meta.env.VITE_APP_API_BASE_URL +
          "/api/spalodWFS/datasets",
        type: "GET",
        dataType: "json",
        success: (response) => {
          console.log(JSON.stringify(response));
          data.value = response.data.results.bindings;
        },
        error: (error) => {
          console.log(error);
        },
      });
    });

    watch(selected, (newValue) => {
      emit("change", newValue);
    });

    return { data, selected };
  },
};
</script>

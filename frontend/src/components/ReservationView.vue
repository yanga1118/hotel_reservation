<template>

<v-data-table
    :headers="headers"
    :items="reservationView"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ReservationView',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "username", value: "username" },
            { text: "orderStatus", value: "orderStatus" },
            { text: "userId", value: "userId" },
            { text: "roomNo", value: "roomNo" },
            { text: "payAmount", value: "payAmount" },
        ],
        reservationView : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/reservationviews')

      this.reservationView = temp.data._embedded.reservationviews;

    },
    methods: {
    }
  }
</script>


<template>

<v-card style="width:300px; margin-left:5%;" outlined>
    <template slot="progress">
      <v-progress-linear
        color="deep-purple"
        height="10"
        indeterminate
      ></v-progress-linear>
    </template>

    <v-img
      style="width:290px; height:150px; border-radius:10px; position:relative; margin-left:5px; top:5px;"
      src="https://cdn.vuetifyjs.com/images/cards/cooking.png"
    ></v-img>

    <v-card-title v-if="value._links">
        Reservation # {{value._links.self.href.split("/")[value._links.self.href.split("/").length - 1]}}
    </v-card-title >
    <v-card-title v-else>
        Reservation
    </v-card-title >

    <v-card-text style = "margin-left:-15px; margin-top:10px;">

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="RoomId" v-model="value.roomId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            RoomId :  {{value.roomId }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="RoomNo" v-model="value.roomNo"/>
          </div>
          <div class="grey--text ml-4" v-else>
            RoomNo :  {{value.roomNo }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="RoomStatus" v-model="value.roomStatus"/>
          </div>
          <div class="grey--text ml-4" v-else>
            RoomStatus :  {{value.roomStatus }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="RoomSize" v-model="value.roomSize"/>
          </div>
          <div class="grey--text ml-4" v-else>
            RoomSize :  {{value.roomSize }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="AmenityInfo" v-model="value.amenityInfo"/>
          </div>
          <div class="grey--text ml-4" v-else>
            AmenityInfo :  {{value.amenityInfo }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.createDate"
                    label="CreateDate"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.createDate"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            CreateDate :  {{value.createDate }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.reservStatus"
                    label="ReservStatus"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.reservStatus"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            ReservStatus :  {{value.reservStatus }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="PayCompltedYn" v-model="value.payCompltedYn"/>
          </div>
          <div class="grey--text ml-4" v-else>
            PayCompltedYn :  {{value.payCompltedYn }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field type="number" label="UserId" v-model="value.userId"/>
          </div>
          <div class="grey--text ml-4" v-else>
            UserId :  {{value.userId }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="UserName" v-model="value.userName"/>
          </div>
          <div class="grey--text ml-4" v-else>
            UserName :  {{value.userName }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="PeopleQty" v-model="value.peopleQty"/>
          </div>
          <div class="grey--text ml-4" v-else>
            PeopleQty :  {{value.peopleQty }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.reservDate"
                    label="ReservDate"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.reservDate"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            ReservDate :  {{value.reservDate }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-text-field label="PayStatus" v-model="value.payStatus"/>
          </div>
          <div class="grey--text ml-4" v-else>
            PayStatus :  {{value.payStatus }}
          </div>

          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.payDate"
                    label="PayDate"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.payDate"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            PayDate :  {{value.payDate }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.reservStartDate"
                    label="ReservStartDate"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.reservStartDate"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            ReservStartDate :  {{value.reservStartDate }}
          </div>
          <div class="grey--text ml-4" v-if="editMode" style = "margin-top:-20px;">
            <v-menu
                v-model="menu"
                width="290px"
            >
                <template v-slot:activator="{ on, attrs }">
                <v-text-field
                    v-model="value.reservEndDate"
                    label="ReservEndDate"
                    prepend-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                ></v-text-field>
                </template>
                <v-date-picker
                v-model="value.reservEndDate"
                :min="new Date().toISOString().substr(0, 10)"
                @input="menu = false"
                ></v-date-picker>
            </v-menu>
          </div>
          <div class="grey--text ml-4" v-else>
            ReservEndDate :  {{value.reservEndDate }}
          </div>

    </v-card-text>

    <v-divider class="mx-4"></v-divider>

    <v-card-actions style = "position:absolute; right:0; bottom:0;">
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="edit"
        v-if="!editMode"
      >
        Edit
      </v-btn>
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="save"
        v-else
      >
        Save
      </v-btn>
      <v-btn
        color="deep-purple lighten-2"
        text
        @click="remove"
        v-if="!editMode"
      >
        Delete
      </v-btn>
    </v-card-actions>
  </v-card>


</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'Reservation',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        date: new Date().toISOString().substr(0, 10),
    }),

    methods: {
      edit(){
        this.editMode = true;
      },
      async save(){
        try{
          var temp = null;

          if(this.isNew){
            temp = await axios.post(axios.fixUrl('/reservations'), this.value)
          }else{
            temp = await axios.put(axios.fixUrl(this.value._links.self.href), this.value)
          }

          this.value = temp.data;

          this.editMode = false;
          this.$emit('input', this.value);

          if(this.isNew){
            this.$emit('add', this.value);
          }else{
            this.$emit('edit', this.value);
          }

        }catch(e){
          alert(e.message)
        }
      },
      async remove(){
        try{
          await axios.delete(axios.fixUrl(this.value._links.self.href))
          this.editMode = false;
          this.isDeleted = true;

          this.$emit('input', this.value);
          this.$emit('delete', this.value);

        }catch(e){
          alert(e.message)
        }
      },

    }
  }
</script>


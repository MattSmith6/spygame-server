package com.github.spygameserver.database.table;



public class game_record {

    //class parameters
    private int record_id;
    private int  game_id;
    private int  eliminator_id;
    private int  eliminatee_id;
    private long elimination_time;

    //Constructor
    public game_record(int record_id, int game_id,     int   eliminator_id,    int     eliminatee_id,    long    elimination_time) {
        this.game_id           = game_id;
        this.eliminator_id     = eliminator_id;
        this.eliminatee_id     = eliminatee_id;
        this.elimination_time  = elimination_time;
        this.record_id = record_id;

    }
    //Getter and Setter of All Parameters
    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public int getEliminator_id() {
        return eliminator_id;
    }

    public void setEliminator_id(int eliminator_id) {
        this.eliminator_id = eliminator_id;
    }

    public int getEliminatee_id() {
        return eliminatee_id;
    }

    public void setEliminatee_id(int eliminatee_id) {
        this.eliminatee_id = eliminatee_id;
    }

    public long getElimination_time() {
        return elimination_time;
    }

    public void setElimination_time(long elimination_time) {
        this.elimination_time = elimination_time;
    }

    public int getRecord_id() {
        return  record_id;
    }
    public void setRecord_id(int record_id){
        this.record_id = record_id;
    }
}

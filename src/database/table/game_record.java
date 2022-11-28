package com.github.spygameserver.database.table;

public class game_record {
    //class parameters
    private int  game_id;
    private int  eliminator_id;
    private int  eliminatee_id;
    private String elimination_time;

    //Constructor
    public game_record(int game_id,     int   eliminator_id,    int     eliminatee_id,    String    elimination_time) {
        this.game_id           = game_id;
        this.eliminator_id     = eliminator_id;
        this.eliminatee_id     = eliminatee_id;
        this.elimination_time  = elimination_time;

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

    public String getElimination_time() {
        return elimination_time;
    }

    public void setElimination_time(String elimination_time) {
        this.elimination_time = elimination_time;
    }

}
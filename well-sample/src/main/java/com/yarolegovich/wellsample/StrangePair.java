package com.yarolegovich.wellsample;

import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.annotation.Column;
import com.yarolegovich.wellsql.core.annotation.PrimaryKey;
import com.yarolegovich.wellsql.core.annotation.RawConstraints;
import com.yarolegovich.wellsql.core.annotation.Table;

@Table
@RawConstraints({"FOREIGN KEY(HERO_ID) REFERENCES SuperHero(_id)", "FOREIGN KEY(VILLAIN_ID) REFERENCES Villain(_id)"})
public class StrangePair implements Identifiable {
    @Column
    @PrimaryKey(autoincrement = false)
    private int id;
    @Column
    private int heroId;
    @Column
    private int villainId;

    public StrangePair() { }

    public StrangePair(int id, int heroId, int villainId) {
        this.id = id;
        this.heroId = heroId;
        this.villainId = villainId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getVillainId() {
        return villainId;
    }

    public void setVillainId(int villainId) {
        this.villainId = villainId;
    }
}

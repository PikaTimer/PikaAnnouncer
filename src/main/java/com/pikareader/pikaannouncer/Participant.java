/*
 * Copyright (C) 2024 John Garner <segfaultcoredump@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pikareader.pikaannouncer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.Callback;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Garner <segfaultcoredump@gmail.com>
 */
public class Participant {

    private final StringProperty firstNameProperty = new SimpleStringProperty("");
    private final StringProperty middleNameProperty = new SimpleStringProperty();
    private final StringProperty lastNameProperty = new SimpleStringProperty("");
    private final StringProperty fullNameProperty = new SimpleStringProperty();
    private final StringProperty bibProperty = new SimpleStringProperty();
    private final StringProperty ageProperty = new SimpleStringProperty();
    private final StringProperty sexProperty = new SimpleStringProperty();
    private final StringProperty cityProperty = new SimpleStringProperty();
    private final StringProperty stateProperty = new SimpleStringProperty();
    private final StringProperty countryProperty = new SimpleStringProperty();
    private final StringProperty noteProperty = new SimpleStringProperty();
    private final StringProperty timeProperty = new SimpleStringProperty();
    private final StringProperty raceProperty = new SimpleStringProperty();
    private final StringProperty crProperty = new SimpleStringProperty();

    private static final Logger logger = LoggerFactory.getLogger(Participant.class);

    ObservableMap attributeMap = FXCollections.observableHashMap();

    public void Participant() {

    }

    public String getBib() {
        return bibProperty.getValue();
    }

    public void setBib(String id) {
        bibProperty.setValue(id);
    }

    public StringProperty bibProperty() {
        return bibProperty;
    }

    private void updateFullName() {
        fullNameProperty.setValue((firstNameProperty.getValueSafe() + " " + middleNameProperty.getValueSafe() + " " + lastNameProperty.getValueSafe()).replaceAll("( )+", " "));
    }

    public static ObservableMap<String, String> getAvailableAttributes() {
        ObservableMap<String, String> attribMap = FXCollections.observableMap(new LinkedHashMap());

        attribMap.put("bib", "Bib");
        attribMap.put("first", "First Name");
        attribMap.put("middle", "Middle Name");
        attribMap.put("last", "Last Name");
        attribMap.put("age", "Age");
        attribMap.put("birth", "Birthday");
        attribMap.put("sex", "Sex");
        attribMap.put("city", "City");
        attribMap.put("state", "State");
        attribMap.put("country", "Country");
        attribMap.put("status", "Status");
        attribMap.put("note", "Note");
        // TODO: routine to add custom attributes based on db lookup
        return attribMap;
    }

    public void setAttributes(Map<String, String> attribMap) {
        // bulk set routine.

        attribMap.entrySet().stream().forEach((Map.Entry<String, String> entry) -> {
            if (entry.getKey() != null) {
                logger.debug("processing " + entry.getKey() + " -> " + entry.getValue());
                switch (entry.getKey().toLowerCase()) {
                    case "bib" ->
                        this.setBib(entry.getValue());
                    case "first" ->
                        this.setFirstName(entry.getValue());
                    case "middle" ->
                        this.setMiddleName(entry.getValue());
                    case "last" ->
                        this.setLastName(entry.getValue());
                    case "age" ->
                        this.setAge(entry.getValue());
                    case "sex", "gender" ->
                        this.setSex(entry.getValue());
                    case "city" ->
                        this.setCity(entry.getValue());
                    case "state" ->
                        this.setState(entry.getValue());
                    case "country" ->
                        this.setCountry(entry.getValue());
                    case "note" ->
                        this.setNote(entry.getValue());
                }
            }
        });

        logger.debug("Added " + (firstNameProperty.getValueSafe() + " " + middleNameProperty.getValueSafe() + " " + lastNameProperty.getValueSafe()).replaceAll("( )+", " "));
        logger.debug("  " + fullNameProperty.getValueSafe());
    }

    public String getFirstName() {
        return firstNameProperty.getValueSafe();
    }

    public void setFirstName(String fName) {
        firstNameProperty.setValue(fName);
        updateFullName();
    }

    public StringProperty firstNameProperty() {
        return firstNameProperty;
    }

    public String getLastName() {
        return lastNameProperty.getValueSafe();
    }

    public void setLastName(String fName) {
        lastNameProperty.setValue(fName);
        updateFullName();
    }

    public StringProperty lastNameProperty() {
        return lastNameProperty;
    }

    public String getMiddleName() {
        return middleNameProperty.getValueSafe();
    }

    public void setMiddleName(String mName) {
        middleNameProperty.setValue(mName);
        updateFullName();
    }

    public StringProperty middleNameProperty() {
        return middleNameProperty;
    }

    public StringProperty fullNameProperty() {
        return fullNameProperty;
    }

    public String getAge() {
        return ageProperty.getValue();
    }

    public void setAge(String a) {
        ageProperty.setValue(a);
    }

    public StringProperty ageProperty() {
        return ageProperty;
    }

    public StringProperty timeProperty() {
        return timeProperty;
    }

    public StringProperty raceProperty() {
        return raceProperty;
    }

    public String getSex() {
        return sexProperty.getValueSafe();
    }

    public void setSex(String s) {
        //Set to an upper case M or F for now
        //TODO: Switch this to the allowable values for a SEX 
        if (s == null) {
            return;
        }
        if (s.startsWith("M") || s.startsWith("m")) {
            sexProperty.setValue("M");
        } else if (s.startsWith("F") || s.startsWith("f")) {
            sexProperty.setValue("F");
        } else {
            sexProperty.setValue(s.toUpperCase());
        }
    }

    public StringProperty sexProperty() {
        return sexProperty;
    }

    public String getCity() {
        return cityProperty.getValueSafe();
    }

    public void setCity(String c) {
        cityProperty.setValue(c);
    }

    public StringProperty cityProperty() {
        return cityProperty;
    }

    public String getState() {
        return stateProperty.getValueSafe();
    }

    public void setState(String s) {
        stateProperty.setValue(s);
    }

    public StringProperty stateProperty() {
        return stateProperty;
    }

    public String getCountry() {
        return countryProperty.getValueSafe();
    }

    public void setCountry(String s) {
        countryProperty.setValue(s);
    }

    public StringProperty countryProperty() {
        return countryProperty;
    }

    public String getNote() {
        return noteProperty.getValueSafe();
    }

    public void setNote(String s) {
        noteProperty.setValue(s);
    }

    public StringProperty noteProperty() {
        return noteProperty;
    }

    public String getCR() {
        return crProperty.getValueSafe();
    }

    public void setCR(String s) {
        crProperty.setValue(s);
    }

    public StringProperty crProperty() {
        return crProperty;
    }

    public void setRaceTime(String race, String time) {
        raceProperty.set(race);
        timeProperty.set(time);
    }

    public static Callback<Participant, Observable[]> extractor() {
        return (Participant p) -> new Observable[]{p.firstNameProperty, p.middleNameProperty, p.lastNameProperty, p.bibProperty, p.ageProperty, p.sexProperty, p.cityProperty, p.stateProperty, p.countryProperty, p.crProperty, p.timeProperty};
    }

    @Override
    public String toString() {
        return fullNameProperty.getValueSafe();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.bibProperty.getValue());

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Participant other = (Participant) obj;
        return Objects.equals(this.bibProperty.getValue(), other.bibProperty.getValue());
    }

    void setFromJSON(JSONObject jsonObject) {
        jsonObject.toMap().keySet().forEach(k -> {
            switch (k) {
                case "Bib" ->
                    this.setBib(jsonObject.optString(k));
                case "FirstName" ->
                    this.setFirstName(jsonObject.optString(k));
                case "MiddleName" ->
                    this.setMiddleName(jsonObject.optString(k));
                case "LastName" ->
                    this.setLastName(jsonObject.optString(k));
                case "Age" ->
                    this.setAge(jsonObject.optString(k));
                case "Sex" ->
                    this.setSex(jsonObject.optString(k));
                case "City" ->
                    this.setCity(jsonObject.optString(k));
                case "State" ->
                    this.setState(jsonObject.optString(k));
                case "Country" ->
                    this.setCountry(jsonObject.optString(k));
                case "Note" ->
                    this.setNote(jsonObject.optString(k));

            }

        });

    }

}

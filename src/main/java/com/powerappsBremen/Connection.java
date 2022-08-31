/**
 * An object that represents an mqqt connection from a client to an mqtt broker
 *  Author Anastasios Fakas 03.03.2022
 *  Version 1.0
 */
package com.powerappsBremen;

import java.util.Arrays;
import java.util.List;

public class Connection {

        private String URL;
        private String List;
        private List<String> Parameters;
        private String Topic;

    public String getTopic() {
        return Topic;
    }

    public void setTopic(String topic) {
        Topic = topic;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getList() {
        return List;
    }

    public void setList(String list) {
        List = list;
    }

    public java.util.List<String> getParameters() {
        return Parameters;
    }

    public void setParameters(java.util.List<String> parameters) {
        Parameters = parameters;
    }

    //+   Arrays.toString(ParametersName.toArray())
        @Override
        public String toString() {
            return "Connection [URL=" + URL + ", Topic=" + Topic + ", List=" + List +    ", Parameters=" +   Arrays.toString(Parameters.toArray())  + "]";
        }

}

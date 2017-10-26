package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete forecasts.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/forecasts
 *
 *
 */
public class Forecast extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Forecast.class);

    /**
     * Constructor
     *
     */
    public Forecast() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Forecast(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }


    /**
     * Constructor
     *
     */
    public Forecast(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is a Forecast
     *
     * @param resource the resource to be checked
     * @return true if it's a Forecast
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(FORECAST_RE);
    }

    /**
     * Creates a forecast from a timeseries.
     *
     * POST /andromeda/forecast?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars for the timeseries to attach
     *            the forecast.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a forecast for.
     * @param args
     *            set of parameters for the new forecast. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the forecast.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String timeSeriesId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (timeSeriesId == null || timeSeriesId.length() == 0 ) {
            logger.info("Wrong timeseries id. Id cannot be null");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .timeSeriesIsReady(timeSeriesId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                inputDataJSON = inputData;
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("timeseries", timeSeriesId);
            requestObject.put("input_data", inputData);

            return createResource(FORECAST_URL,
                                  requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating timeseries");
            return null;
        }
    }

    /**
     * Retrieves a forecast.
     *
     * GET /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String forecastId) {
        if (forecastId == null || forecastId.length() == 0
                || !forecastId.matches(FORECAST_RE)) {
            logger.info("Wrong forecast id");
            return null;
        }

        return getResource(BIGML_URL + forecastId);
    }

    /**
     * Retrieves a forecast.
     *
     * GET /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param forecast
     *            a forecast JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject forecast) {
        String forecastId = (String) forecast.get("resource");
        return get(forecastId);
    }


    /**
     * Checks whether a forecast's status is FINISHED.
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id
     *            is a stringof 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String forecastId) {
        return isResourceReady(get(forecastId));
    }

    /**
     * Checks whether a forecast's status is FINISHED.
     *
     * @param forecast  an forecast JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject forecast) {
        return isResourceReady(forecast)
                || isReady((String) forecast.get("resource"));
    }

    /**
     * Lists all your forecasts.
     *
     * GET /andromeda/forecast?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(FORECAST_URL, queryString);
    }

    /**
     * Updates a forecast.
     *
     * PUT /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the forecast. Optional
     *
     */
    @Override
    public JSONObject update(final String forecastId, final String changes) {
        if (forecastId == null || forecastId.length() == 0
                || !(forecastId.matches(FORECAST_RE))) {
            logger.info("Wrong forecast id");
            return null;
        }
        return updateResource(BIGML_URL + forecastId, changes);
    }

    /**
     * Updates a forecast.
     *
     * PUT /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param forecast
     *            a forecast JSONObject
     * @param changes
     *            set of parameters to update the forecast. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject forecast, final JSONObject changes) {
        String resourceId = (String) forecast.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a forecast.
     *
     * DELETE
     * /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String forecastId) {
        if (forecastId == null || forecastId.length() == 0
                || !(forecastId.matches(FORECAST_RE))) {
            logger.info("Wrong forecast id");
            return null;
        }
        return deleteResource(BIGML_URL + forecastId);
    }

    /**
     * Deletes a forecast.
     *
     * DELETE
     * /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param forecast
     *            a forecast JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject forecast) {
        String resourceId = (String) forecast.get("resource");
        return delete(resourceId);
    }

}

package net.f4fs.util;

import net.f4fs.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * URL builder helper. Uses config file for the parts.
 *
 * Created by samuel on 31.03.15.
 */
public class URLBuilder {
    public static class Builder {
        private String        _protocol = "";
        private String        _host = "";
        private int           _port = -1; // To check if set or not
        private String        _path;
        private String        _queryString;
        private Map<String, String> _queryParams;

        public Builder() {}

        public Builder(Config config) {
            _protocol = Config.DEFAULT.getProtocol();
            _host     = Config.DEFAULT.getHost();

            _queryParams = new HashMap<>();
            _queryParams.put("token", Config.DEFAULT.getAuthToken());
        }

        /**
         * Defines the used protocol or scheme.
         *
         * @param protocol
         * @return
         */
        public Builder protocol(String protocol) {
            _protocol = protocol;

            return this;
        }

        public Builder host(String host) {
            _host = host;

            return this;
        }

        public Builder port(int port) {
            _port = port;

            return this;
        }

        public Builder path(String path) {
            _path = path;

            return this;
        }

        /**
         * Defines the whole query string. If this is set the added query strings will be ignored.
         * If you are not sure how to build the string, use `addQueryParam` instead.
         *
         * @param queryString
         * @return
         */
        public Builder queryString(String queryString) {
            _queryString = queryString;

            return this;
        }
        
        public Builder addQueryParam(String key, String value) {
            getQueryParams().put(key, value);

            return this;
        }

        /**
         * Just another helper. You can always use `addQueryParam`
         *
         * @param token
         * @return
         */
        public Builder appendAuthToken(String token) {
            getQueryParams().put("token", token);

            return this;
        }

        public Builder appendAuthToken() {
            return this.appendAuthToken(Config.DEFAULT.getAuthToken());
        }

        public String build() {
            StringBuilder sb = new StringBuilder();

            sb.append(_protocol).append("://").append(_host);

            if (_port >= 0) {
                sb.append(":").append(_port);
            }

            if (_path != null) {
                sb.append("/").append(_path);
            }

            // Adding the queries parameters
            if (!getQueryParams().isEmpty()) {
                sb.append("?");

                ArrayList<String> params = new ArrayList<>(getQueryParams().size());

                getQueryParams().entrySet().stream().forEach((entry) -> {
                    params.add(entry.getKey() + "=" + entry.getValue());
                });

                sb.append(String.join("&", params));
            }

            return sb.toString();
        }

        private Map<String, String> getQueryParams() {
            if (_queryParams == null) {
                _queryParams = new HashMap<>();
            }

            return _queryParams;
        }
    }
}

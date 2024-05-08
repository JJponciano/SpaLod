FROM node:16

WORKDIR /app

# Copy the package.json and install dependencies
COPY src/main/vue_js/spalod/package*.json ./
RUN npm install

# Copy the rest of the frontend source code
COPY src/main/vue_js/spalod .

# Build the application
RUN npm run build

# Serve the app using serve
RUN npm install -g serve
CMD ["serve", "-s", "dist", "-l", "8081"]
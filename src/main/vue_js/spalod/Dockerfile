# Stage 1: Build the Vite application
FROM node:latest AS build
WORKDIR /app

# Copy the package.json and package-lock.json files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the application files
COPY . .

# Build the application
RUN npm run build

# Stage 2: Create the production-ready image with NGINX
FROM nginx:latest

# Copy the built files from the previous stage to the NGINX web root directory
COPY --from=build /app/dist /usr/share/nginx/html
COPY ./default.conf /etc/nginx/conf.d/default.conf


# Expose the default NGINX port
EXPOSE 80

# Start NGINX
CMD ["nginx", "-g", "daemon off;"]

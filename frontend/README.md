# ascient-ui
The frontend for ascient, providing a React UI for accessing ascient-backend. 

## Getting Started

### Prerequisites
Ascient-ui requires a recent version of Yarn and NodeJS. Install dependencies before running:
```shell
yarn install
```

### Running
Use Yarn to start the development server:
```shell
yarn start
```
The server will listen at `http://localhost:3000`. API requests will be proxied to `http://localhost:8080`.

### Building
A production-ready, minified build can be created with the `build` task.
```shell
yarn build
```

### Deployment
Refer to the [deployment](https://facebook.github.io/create-react-app/docs/deployment) documentation for more information on production deployment.

Alternatively, the UI can run in a Docker container:
```shell
docker build -t ascient-ui
docker run -d --name ascient-ui -p [UI_PORT]:80 -eBACKEND_URL=[BACKEND_HOST] -eBACKEND_PORT=[BACKEND_PORT] ascient-ui
```

## More Info
This frontend is based on [Create React App](https://github.com/facebook/create-react-app). For more information refer to the Create [React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).


## Built With
* [Create React App](https://github.com/facebook/create-react-app)
* [Redux Starter Kit](https://github.com/reduxjs/redux-starter-kit) and [React Redux](https://github.com/reduxjs/react-redux)
* [React Bootstrap](https://react-bootstrap.github.io/)

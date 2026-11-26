set -eu

container=$(docker run -d --platform "$CI_PLATFORM" -p "127.0.0.1::$CI_CONTAINER_PORT" "$CI_IMAGE")
trap 'docker rm -f "$container" >/dev/null' EXIT

binding=$(docker port "$container" "$CI_CONTAINER_PORT/tcp")
export SMOKE_BASE_URL="http://$binding"

if ! sh -eu -c "$CI_SMOKE_COMMAND"; then
    docker logs "$container"
    exit 1
fi

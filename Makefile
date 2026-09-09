.PHONY: test backend frontend up down

test:
	cd backend && mvn -q test
	cd frontend && npm test

backend:
	cd backend && mvn spring-boot:run

frontend:
	cd frontend && npm run dev

up:
	docker compose up --build

down:
	docker compose down

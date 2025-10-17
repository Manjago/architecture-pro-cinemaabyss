#!/usr/bin/env python3
from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import sys
from threading import Thread

class MonolithHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith('/api/movies'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {
                "source": "MONOLITH",
                "movies": [
                    {"id": 1, "title": "The Godfather (from monolith)"},
                    {"id": 2, "title": "Pulp Fiction (from monolith)"}
                ]
            }
            self.wfile.write(json.dumps(response, indent=2).encode())
        else:
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(f"Monolith: {self.path}".encode())
    
    def log_message(self, format, *args):
        print(f"[MONOLITH:8080] {args[0]}")

class MoviesHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith('/api/movies'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {
                "source": "MOVIES-SERVICE",
                "movies": [
                    {"id": 1, "title": "Inception (from movies-service)"},
                    {"id": 2, "title": "Interstellar (from movies-service)"}
                ]
            }
            self.wfile.write(json.dumps(response, indent=2).encode())
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b"Movies service: only /api/movies available")
    
    def log_message(self, format, *args):
        print(f"[MOVIES:8081] {args[0]}")

def run_monolith():
    server = HTTPServer(('localhost', 8080), MonolithHandler)
    print("✅ Monolith server running on http://localhost:8080")
    server.serve_forever()

def run_movies():
    server = HTTPServer(('localhost', 8081), MoviesHandler)
    print("✅ Movies service running on http://localhost:8081")
    server.serve_forever()

if __name__ == '__main__':
    Thread(target=run_monolith, daemon=True).start()
    Thread(target=run_movies, daemon=True).start()
    
    print("\n🚀 Mock servers started!")
    print("Press Ctrl+C to stop\n")
    
    try:
        while True:
            pass
    except KeyboardInterrupt:
        print("\n👋 Shutting down...")
        sys.exit(0)
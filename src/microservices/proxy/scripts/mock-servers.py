#!/usr/bin/env python3
from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import sys
from threading import Thread

class MonolithHandler(BaseHTTPRequestHandler):
    def _handle_movies(self, method):
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length).decode('utf-8') if content_length > 0 else None
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        
        response = {
            "source": "MONOLITH",
            "method": method,
            "path": self.path,
            "receivedBody": body
        }
        
        if method == "GET":
            response["movies"] = [
                {"id": 1, "title": "The Godfather (from monolith)"},
                {"id": 2, "title": "Pulp Fiction (from monolith)"}
            ]
        elif method == "POST":
            response["message"] = "Movie created in monolith"
        elif method == "PUT":
            response["message"] = "Movie updated in monolith"
        elif method == "DELETE":
            response["message"] = "Movie deleted in monolith"
            
        self.wfile.write(json.dumps(response, indent=2).encode())
    
    def do_GET(self):
        if self.path.startswith('/api/movies'):
            self._handle_movies("GET")
        else:
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(f"Monolith: {self.path}".encode())
    
    def do_POST(self):
        self._handle_movies("POST")
    
    def do_PUT(self):
        self._handle_movies("PUT")
    
    def do_DELETE(self):
        self._handle_movies("DELETE")
    
    def do_PATCH(self):
        self._handle_movies("PATCH")
    
    def log_message(self, format, *args):
        print(f"[MONOLITH:8080] {args[0]}")

class MoviesHandler(BaseHTTPRequestHandler):
    def _handle_movies(self, method):
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length).decode('utf-8') if content_length > 0 else None
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        
        response = {
            "source": "MOVIES-SERVICE",
            "method": method,
            "path": self.path,
            "receivedBody": body
        }
        
        if method == "GET":
            response["movies"] = [
                {"id": 1, "title": "Inception (from movies-service)"},
                {"id": 2, "title": "Interstellar (from movies-service)"}
            ]
        elif method == "POST":
            response["message"] = "Movie created in movies-service"
        elif method == "PUT":
            response["message"] = "Movie updated in movies-service"
        elif method == "DELETE":
            response["message"] = "Movie deleted in movies-service"
            
        self.wfile.write(json.dumps(response, indent=2).encode())
    
    def do_GET(self):
        if self.path.startswith('/api/movies'):
            self._handle_movies("GET")
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b"Movies service: only /api/movies available")
    
    def do_POST(self):
        self._handle_movies("POST")
    
    def do_PUT(self):
        self._handle_movies("PUT")
    
    def do_DELETE(self):
        self._handle_movies("DELETE")
    
    def do_PATCH(self):
        self._handle_movies("PATCH")
    
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
#!/bin/bash

# Script to migrate javax.* to jakarta.* namespace
# This script performs a find-and-replace across all Java files

echo "Starting javax.* to jakarta.* migration..."

# Find all Java files and replace javax imports with jakarta
find auth-service/src game-service/src player-service/src -name "*.java" -type f -exec sed -i '' \
  -e 's/import javax\.annotation\./import jakarta.annotation./g' \
  -e 's/import javax\.enterprise\./import jakarta.enterprise./g' \
  -e 's/import javax\.inject\./import jakarta.inject./g' \
  -e 's/import javax\.json\./import jakarta.json./g' \
  -e 's/import javax\.naming\./import jakarta.naming./g' \
  -e 's/import javax\.servlet\./import jakarta.servlet./g' \
  -e 's/import javax\.sql\./import jakarta.sql./g' \
  -e 's/import javax\.validation\./import jakarta.validation./g' \
  -e 's/import javax\.websocket\./import jakarta.websocket./g' \
  -e 's/import javax\.ws\.rs\./import jakarta.ws.rs./g' \
  {} \;

echo "Migration complete!"
echo "Files modified:"
find auth-service/src game-service/src player-service/src -name "*.java" -type f | wc -l

# Made with Bob

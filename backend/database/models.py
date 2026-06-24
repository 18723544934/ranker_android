from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship

Base = declarative_base()


class Hardware(Base):
    __tablename__ = "hardwares"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False, index=True)
    brand = Column(String(100), nullable=False, index=True)
    category = Column(String(50), nullable=False, index=True)
    architecture = Column(String(100))
    launch_date = Column(DateTime)
    specs_json = Column(Text)
    overall_score = Column(Float, nullable=False, index=True)
    price_info_json = Column(Text)
    image_url = Column(String(500))
    is_favorite = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    benchmarks = relationship("Benchmark", back_populates="hardware", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Hardware(id={self.id}, name={self.name}, brand={self.brand})>"


class Benchmark(Base):
    __tablename__ = "benchmarks"

    id = Column(Integer, primary_key=True, index=True)
    hardware_id = Column(Integer, ForeignKey("hardwares.id", ondelete="CASCADE"), nullable=False)
    source = Column(String(100), nullable=False, index=True)
    metric = Column(String(100), nullable=False)
    score = Column(Float, nullable=False)
    unit = Column(String(50))
    created_at = Column(DateTime, default=datetime.utcnow)

    hardware = relationship("Hardware", back_populates="benchmarks")

    def __repr__(self):
        return f"<Benchmark(hardware_id={self.hardware_id}, source={self.source}, score={self.score})>"


class Favorite(Base):
    __tablename__ = "favorites"

    id = Column(Integer, primary_key=True, index=True)
    hardware_id = Column(Integer, ForeignKey("hardwares.id", ondelete="CASCADE"), nullable=False, index=True)
    group_name = Column(String(100), default="默认")
    created_at = Column(DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<Favorite(hardware_id={self.hardware_id}, group={self.group_name})>"


class History(Base):
    __tablename__ = "history"

    id = Column(Integer, primary_key=True, index=True)
    hardware_id = Column(Integer, ForeignKey("hardwares.id", ondelete="CASCADE"), nullable=False, index=True)
    visited_at = Column(DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<History(hardware_id={self.hardware_id}, visited_at={self.visited_at})>"

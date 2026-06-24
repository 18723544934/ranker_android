from fastapi import FastAPI, HTTPException, Query, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from database.connection import get_db, engine, Base
from database.models import Hardware, Benchmark
from pydantic import BaseModel
import redis
import os
import json

# Create tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="PerfTop API",
    description="Android 性能排行榜后端 API",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Redis client
redis_client = redis.Redis(
    host=os.getenv("REDIS_HOST", "localhost"),
    port=int(os.getenv("REDIS_PORT", 6379)),
    db=0,
    decode_responses=True
)


# Pydantic models
class SpecsDto(BaseModel):
    cores: Optional[int] = None
    threads: Optional[int] = None
    baseClockGHz: Optional[float] = None
    boostClockGHz: Optional[float] = None
    tdpWatts: Optional[int] = None
    lithography: Optional[str] = None
    vramGB: Optional[int] = None
    memoryType: Optional[str] = None
    bandwidthGBs: Optional[float] = None
    cache: Optional[str] = None


class BenchmarkDto(BaseModel):
    source: str
    metric: str
    score: float
    unit: str


class PriceInfoDto(BaseModel):
    currency: str
    amount: float
    source: str
    updated: Optional[str] = None


class HardwareDto(BaseModel):
    id: int
    name: str
    brand: str
    category: str
    architecture: str
    launchDate: Optional[str] = None
    specs: Optional[SpecsDto] = None
    overallScore: float
    benchmarks: List[BenchmarkDto] = []
    priceInfo: Optional[PriceInfoDto] = None
    imageUrl: Optional[str] = None


class HardwareListResponse(BaseModel):
    data: List[HardwareDto]
    page: int
    perPage: int
    total: int


class HardwareDetailResponse(BaseModel):
    data: HardwareDto


class CompareResponse(BaseModel):
    data: List[HardwareDto]
    comparison: dict


class FilterOptionsResponse(BaseModel):
    brands: List[str]
    architectures: List[str]
    coreRanges: List[str]
    frequencyRanges: List[str]
    years: List[int]


@app.get("/")
async def root():
    return {"message": "PerfTop API is running", "version": "1.0.0"}


@app.get("/health")
async def health_check():
    return {"status": "healthy", "database": "connected"}


@app.get("/hardwares", response_model=HardwareListResponse)
async def get_hardwares(
    category: str,
    sort_by: str = "overall_score",
    order: str = "desc",
    page: int = Query(1, ge=1),
    per_page: int = Query(20, ge=1, le=100),
    brand: Optional[str] = None,
    architecture: Optional[str] = None,
    db: Session = Depends(get_db)
):
    query = db.query(Hardware).filter(Hardware.category == category)

    if brand:
        query = query.filter(Hardware.brand == brand)
    if architecture:
        query = query.filter(Hardware.architecture == architecture)

    # Sorting
    order_column = getattr(Hardware, sort_by, Hardware.overall_score)
    if order == "desc":
        query = query.order_by(order_column.desc())
    else:
        query = query.order_by(order_column.asc())

    # Pagination
    total = query.count()
    items = query.offset((page - 1) * per_page).limit(per_page).all()

    # Convert to DTO
    data = []
    for item in items:
        benchmarks = [
            BenchmarkDto(
                source=b.source,
                metric=b.metric,
                score=b.score,
                unit=b.unit
            )
            for b in item.benchmarks
        ]
        data.append(HardwareDto(
            id=item.id,
            name=item.name,
            brand=item.brand,
            category=item.category,
            architecture=item.architecture,
            launchDate=item.launch_date.isoformat() if item.launch_date else None,
            specs=json.loads(item.specs_json) if item.specs_json else None,
            overallScore=item.overall_score,
            benchmarks=benchmarks,
            priceInfo=json.loads(item.price_info_json) if item.price_info_json else None,
            imageUrl=item.image_url
        ))

    return HardwareListResponse(
        data=data,
        page=page,
        perPage=per_page,
        total=total
    )


@app.get("/hardwares/{hardware_id}", response_model=HardwareDetailResponse)
async def get_hardware_detail(hardware_id: int, db: Session = Depends(get_db)):
    hardware = db.query(Hardware).filter(Hardware.id == hardware_id).first()
    if not hardware:
        raise HTTPException(status_code=404, detail="Hardware not found")

    benchmarks = [
        BenchmarkDto(
            source=b.source,
            metric=b.metric,
            score=b.score,
            unit=b.unit
        )
        for b in hardware.benchmarks
    ]

    return HardwareDetailResponse(
        data=HardwareDto(
            id=hardware.id,
            name=hardware.name,
            brand=hardware.brand,
            category=hardware.category,
            architecture=hardware.architecture,
            launchDate=hardware.launch_date.isoformat() if hardware.launch_date else None,
            specs=json.loads(hardware.specs_json) if hardware.specs_json else None,
            overallScore=hardware.overall_score,
            benchmarks=benchmarks,
            priceInfo=json.loads(hardware.price_info_json) if hardware.price_info_json else None,
            imageUrl=hardware.image_url
        )
    )


@app.get("/hardwares/compare", response_model=CompareResponse)
async def compare_hardwares(
    ids: str = Query(..., description="Comma-separated hardware IDs"),
    db: Session = Depends(get_db)
):
    hardware_ids = [int(id) for id in ids.split(",")]
    hardwares = db.query(Hardware).filter(Hardware.id.in_(hardware_ids)).all()

    if not hardwares:
        raise HTTPException(status_code=404, detail="No hardwares found")

    data = []
    for hardware in hardwares:
        benchmarks = [
            BenchmarkDto(
                source=b.source,
                metric=b.metric,
                score=b.score,
                unit=b.unit
            )
            for b in hardware.benchmarks
        ]
        data.append(HardwareDto(
            id=hardware.id,
            name=hardware.name,
            brand=hardware.brand,
            category=hardware.category,
            architecture=hardware.architecture,
            launchDate=hardware.launch_date.isoformat() if hardware.launch_date else None,
            specs=json.loads(hardware.specs_json) if hardware.specs_json else None,
            overallScore=hardware.overall_score,
            benchmarks=benchmarks,
            priceInfo=json.loads(hardware.price_info_json) if hardware.price_info_json else None,
            imageUrl=hardware.image_url
        ))

    # Comparison data
    comparison = {
        "overall_score": {h.id: h.overall_score for h in hardwares},
        "brand": {h.id: h.brand for h in hardwares},
        "architecture": {h.id: h.architecture for h in hardwares}
    }

    return CompareResponse(data=data, comparison=comparison)


@app.get("/hardwares/search", response_model=HardwareListResponse)
async def search_hardwares(
    q: str,
    category: Optional[str] = None,
    page: int = Query(1, ge=1),
    per_page: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db)
):
    query = db.query(Hardware).filter(
        (Hardware.name.ilike(f"%{q}%")) | (Hardware.brand.ilike(f"%{q}%"))
    )

    if category:
        query = query.filter(Hardware.category == category)

    total = query.count()
    items = query.offset((page - 1) * per_page).limit(per_page).all()

    data = []
    for item in items:
        benchmarks = [
            BenchmarkDto(
                source=b.source,
                metric=b.metric,
                score=b.score,
                unit=b.unit
            )
            for b in item.benchmarks
        ]
        data.append(HardwareDto(
            id=item.id,
            name=item.name,
            brand=item.brand,
            category=item.category,
            architecture=item.architecture,
            launchDate=item.launch_date.isoformat() if item.launch_date else None,
            specs=json.loads(item.specs_json) if item.specs_json else None,
            overallScore=item.overall_score,
            benchmarks=benchmarks,
            priceInfo=json.loads(item.price_info_json) if item.price_info_json else None,
            imageUrl=item.image_url
        ))

    return HardwareListResponse(
        data=data,
        page=page,
        perPage=per_page,
        total=total
    )


@app.get("/meta/filters", response_model=FilterOptionsResponse)
async def get_filter_options(
    category: str,
    db: Session = Depends(get_db)
):
    brands = [row[0] for row in db.query(Hardware.brand).filter(Hardware.category == category).distinct().all()]
    architectures = [row[0] for row in db.query(Hardware.architecture).filter(Hardware.category == category).filter(Hardware.architecture.isnot(None)).distinct().all()]
    years = sorted(set(
        row[0].year for row in db.query(Hardware.launch_date).filter(Hardware.category == category).filter(Hardware.launch_date.isnot(None)).all() if row[0]
    ))

    return FilterOptionsResponse(
        brands=brands,
        architectures=architectures,
        coreRanges=["1-4", "4-8", "8-16", "16+"],
        frequencyRanges=["<2.0", "2.0-3.0", "3.0-4.0", "4.0-5.0", ">5.0"],
        years=years
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

# 클래스 다이어그램

## 상품 목록 조회

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
    }

    class Brand {
        -id: Long
        -name: String
        -description: String
    }

    class ViewedProduct {
        -id: Long
        -viewedCount: Long
        -product: Product
        -user: User
    }

    class LikedProduct {
        -id: Long
        -product: Product
        -user: User
    }

    Product "N" --> Brand: 참조
    ViewedProduct "N" --> Product: 참조
    ViewedProduct "N" --> User: 참조
    LikedProduct "N" --> Product: 참조
    LikedProduct "N" --> User: 참조
    User ..> ViewedProduct: 조회수 증가
    User ..> LikedProduct: 좋아요
```

## 상품 상세 조회

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
        -options: Option[]
    }

    class ProductOption {
        -id: Long
        -name: String
        -additionalPrice: Long
        -product: Product
    }

    class Brand {
        -id: Long
        -name: String
        -description: String
    }

    class Stock {
        -id: Long
        -quantity: Long
        -productOption: ProductOption
    }

    class ViewedProduct {
        -id: Long
        -viewedCount: Long
        -product: Product
        -user: User
        +increase(count: Long)
    }

    class LikedProduct {
        -id: Long
        -product: Product
        -user: User
    }

    Product "N" --> Brand: 참조
    Product --> "N" ProductOption: 소유
    Stock --> ProductOption: 참조
    ViewedProduct "N" --> Product: 참조
    ViewedProduct "N" --> User: 참조
    LikedProduct "N" --> Product: 참조
    LikedProduct "N" --> User: 참조
    User ..> ViewedProduct: 조회수 증가
    User ..> LikedProduct: 좋아요
```

## 브랜드 상세 조회

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
        -options: Option[]
    }

    class Brand {
        -id: Long
        -name: String
        -description: String
    }

    class LikedProduct {
        -id: Long
        -product: Product
        -user: User
    }

    Product "N" --> Brand: 참조
    LikedProduct "N" --> Product: 참조
    LikedProduct "N" --> User: 참조
    User ..> LikedProduct: 좋아요
```

## 상품 좋아요 등록

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
        -options: Option[]
    }

    class LikedProduct {
        -id: Long
        -product: Product
        -user: User
    }

    User ..> LikedProduct: 좋아요
    LikedProduct "N" --> Product: 참조
    LikedProduct "N" --> User: 참조
```

## 상품 좋아요 취소

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
        -options: Option[]
    }

    class LikedProduct {
        -id: Long
        -product: Product
        -user: User
    }

    User ..> LikedProduct: 좋아요
    LikedProduct "N" --> Product: 참조
    LikedProduct "N" --> User: 참조
```

## 주문 생성

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Product {
        -id: Long
        -name: String
        -basePrice: Long
        -brand: Brand
        -options: Option[]
    }

    class ProductOption {
        -id: Long
        -name: String
        -additionalPrice: Long
        -product: Product
    }

    class Stock {
        -id: Long
        -quantity: Long
        -productOption: ProductOption
        +isEnough(amount: Long) boolean
    }

    class Point {
        -id: Long
        -balance: Long
        -user: User
        +spend(amount: Long)
    }

    class Order {
        -id: String
        -totalPrice: Long
        -status: OrderStatus
        -user: User
        -productOptions: OrderProductOption[]
    }

    class OrderProductOption {
        -id: Long
        -price: Long
        -quantity: Long
        -order: Order
        -productOption: ProductOption
    }

    Product --> "N" ProductOption: 소유
    Stock --> ProductOption: 참조
    Order "N" --> User: 참조
    Order --> "N" OrderProductOption: 소유
    OrderProductOption "N" --> ProductOption: 참조
    Point --> User: 참조
    User ..> Order: 주문
```

## 결제 요청

```mermaid
classDiagram
    class User {
        -id: Long
        -name: String
        -gender: Gender
        -birthDate: LocalDate
        -email: Email
    }

    class Stock {
        -id: Long
        -quantity: Long
        -productOption: ProductOption
        +decrease(amount: Long)
    }

    class Point {
        -id: Long
        -balance: Long
        -user: User
        +spend(amount: Long)
    }

    class Order {
        -id: String
        -totalPrice: Long
        -status: OrderStatus
        -user: User
        +complete()
    }

    class Payment {
        -id: Long
        -amount: Long
        -status: PaymentStatus
        -method: PaymentMethod
        -order: Order
        -user: User
    }

    Payment --> Order: 주문완료
    Payment ..> Stock: 출고
    Payment ..> Point: 소비
    Point --> User: 참조
    User ..> "N" Payment: 결제
```

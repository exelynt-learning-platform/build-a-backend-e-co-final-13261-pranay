package com.pranay.ecommerce_backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import com.pranay.ecommerce_backend.dto.cart.AddCartItemRequest;
import com.pranay.ecommerce_backend.dto.cart.CartResponse;
import com.pranay.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.pranay.ecommerce_backend.entity.*;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.CartItemRepository;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.ProductRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addItemShouldReturnUpdatedCart() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Product product = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(BigDecimal.valueOf(25))
                .stockQuantity(10)
                .build();
        Cart cart = Cart.builder().id(3L).user(user).items(new ArrayList<>()).build();

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(2L);
        request.setQuantity(2);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.findDetailedByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(3L, 2L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(100L);
            return item;
        });

        CartResponse response = cartService.addItem(user.getEmail(), request);

        assertEquals(BigDecimal.valueOf(50), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void updateItemQuantityShouldRejectExcessStock() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Product product = Product.builder().id(2L).name("Mouse").price(BigDecimal.TEN).stockQuantity(1).build();
        Cart cart = Cart.builder().id(3L).user(user).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(4L).cart(cart).product(product).quantity(1).build();

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(4L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> cartService.updateItemQuantity(user.getEmail(), 4L, request));
    }

    @Test
    void removeItemShouldDeleteCartItem() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Product product = Product.builder().id(2L).name("Mouse").price(BigDecimal.TEN).stockQuantity(3).build();
        Cart cart = Cart.builder().id(3L).user(user).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(4L).cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(4L)).thenReturn(Optional.of(item));

        cartService.removeItem(user.getEmail(), 4L);

        verify(cartItemRepository).delete(item);
    }
}
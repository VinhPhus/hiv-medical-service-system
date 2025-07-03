package com.hivcare.controller;

import com.hivcare.entity.ArvProtocol;
import com.hivcare.service.ArvProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/arvProtocol")
public class ArvProtocolWebController {

    private static final Logger logger = LoggerFactory.getLogger(ArvProtocolWebController.class);

    @Autowired
    private ArvProtocolService arvProtocolService;

    @GetMapping("/list")
    public String listProtocols(Model model) {
        try {
            model.addAttribute("protocols", arvProtocolService.getAllProtocols());
            model.addAttribute("totalProtocols", arvProtocolService.getTotalProtocols());
            model.addAttribute("activeProtocols", arvProtocolService.getActiveProtocolsCount());
            model.addAttribute("inactiveProtocols", 
                arvProtocolService.getTotalProtocols() - arvProtocolService.getActiveProtocolsCount());
            return "arvProtocol/list";
        } catch (Exception e) {
            logger.error("Error loading protocol list", e);
            model.addAttribute("error", "Lỗi khi tải danh sách phác đồ ARV");
            return "error/500";
        }
    }

    @GetMapping("/form")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String showForm(Model model) {
        try {
            if (!model.containsAttribute("protocol")) {
                model.addAttribute("protocol", new ArvProtocol());
            }
            return "arvProtocol/form";
        } catch (Exception e) {
            logger.error("Error showing protocol form", e);
            model.addAttribute("error", "Lỗi khi hiển thị form phác đồ ARV");
            return "error/500";
        }
    }

    @GetMapping("/form/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            if (!model.containsAttribute("protocol")) {
                arvProtocolService.getProtocolById(id).ifPresent(protocol -> {
                    model.addAttribute("protocol", protocol);
                });
            }
            return "arvProtocol/form";
        } catch (Exception e) {
            logger.error("Error showing edit form for protocol ID: {}", id, e);
            model.addAttribute("error", "Lỗi khi hiển thị form chỉnh sửa phác đồ ARV");
            return "error/500";
        }
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        try {
            arvProtocolService.getProtocolById(id).ifPresent(protocol -> {
                model.addAttribute("protocol", protocol);
            });
            return "arvProtocol/detail";
        } catch (Exception e) {
            logger.error("Error showing detail for protocol ID: {}", id, e);
            model.addAttribute("error", "Lỗi khi hiển thị chi tiết phác đồ ARV");
            return "error/500";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String saveProtocol(@Valid @ModelAttribute("protocol") ArvProtocol protocol,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.protocol", bindingResult);
                redirectAttributes.addFlashAttribute("protocol", protocol);
                return "redirect:/arvProtocol/form";
            }

            if (protocol.getId() == null) {
                // Create new protocol
                ArvProtocol savedProtocol = arvProtocolService.createProtocol(protocol);
                redirectAttributes.addFlashAttribute("successMessage", "Đã tạo phác đồ ARV mới thành công");
                return "redirect:/arvProtocol/detail/" + savedProtocol.getId();
            } else {
                // Update existing protocol
                ArvProtocol updatedProtocol = arvProtocolService.updateProtocol(protocol.getId(), protocol);
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật phác đồ ARV thành công");
                return "redirect:/arvProtocol/detail/" + updatedProtocol.getId();
            }
        } catch (Exception e) {
            logger.error("Error saving protocol", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu phác đồ ARV: " + e.getMessage());
            redirectAttributes.addFlashAttribute("protocol", protocol);
            return "redirect:/arvProtocol/form" + (protocol.getId() != null ? "/" + protocol.getId() : "");
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String activateProtocol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            arvProtocolService.activateProtocol(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt phác đồ ARV thành công");
        } catch (Exception e) {
            logger.error("Error activating protocol ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi kích hoạt phác đồ ARV");
        }
        return "redirect:/arvProtocol/list";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String deactivateProtocol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            arvProtocolService.deactivateProtocol(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa phác đồ ARV thành công");
        } catch (Exception e) {
            logger.error("Error deactivating protocol ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi vô hiệu hóa phác đồ ARV");
        }
        return "redirect:/arvProtocol/list";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProtocol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            arvProtocolService.deleteProtocol(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phác đồ ARV thành công");
        } catch (Exception e) {
            logger.error("Error deleting protocol ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa phác đồ ARV");
        }
        return "redirect:/arvProtocol/list";
    }
} 